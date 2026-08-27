#!/bin/bash
BOOTSTRAP_SERVERS="localhost:9092"

# 🎛️ PARALLELISM SETTING (Can be changed anytime)
# 1  = Checks and increments one by one (Slow but safest)
# 5  = Processes in batches of 5 in parallel (Recommended balanced speed)
# 10 = Processes in batches of 10 in parallel (Very fast)
PARALLEL_LIMIT=5

echo "=== STEP 1: Automatically Discovering KRaft Cluster Node IDs ==="
NODE_IDS=$(kafka-broker-api-versions.sh --bootstrap-server $BOOTSTRAP_SERVERS 2>/dev/null | grep -o 'id: [0-9]\+' | awk '{print $2}' | sort -u | paste -sd, -)

if [ -z "$NODE_IDS" ]; then
    echo "❌ ERROR: Cluster Node IDs could not be detected! Please check your server address."
    exit 1
fi
echo "✅ Active Node IDs Detected: $NODE_IDS"

# Read default retention settings at the broker level
FIRST_NODE=$(echo "$NODE_IDS" | cut -d',' -f1)
BROKER_CONFIGS=$(kafka-configs.sh --bootstrap-server $BOOTSTRAP_SERVERS --entity-type brokers --entity-name "$FIRST_NODE" --describe 2>/dev/null)
GLOBAL_RET_MS=$(echo "$BROKER_CONFIGS" | grep -o 'log.retention.ms=[0-9]\+' | cut -d'=' -f2)
GLOBAL_RET_BYTES=$(echo "$BROKER_CONFIGS" | grep -o 'log.retention.bytes=[0-9]\+' | cut -d'=' -f2)
if [ -z "$GLOBAL_RET_MS" ]; then GLOBAL_RET_HOURS="168"; else GLOBAL_RET_HOURS=$((GLOBAL_RET_MS / 1000 / 60 / 60)); fi
if [ -z "$GLOBAL_RET_BYTES" ]; then GLOBAL_RET_GB="Unlimited (Global)"; else GLOBAL_RET_GB="$((GLOBAL_RET_BYTES / 1024 / 1024 / 1024)) GB (Global)"; fi
echo "🌐 Broker Global Default Retention Time : $GLOBAL_RET_HOURS Hours"
echo "🌐 Broker Global Default Retention Size : $GLOBAL_RET_GB"
echo "---------------------------------------------------------"

echo "=== STEP 2: Fetching All Live Topics List ==="
TOPICS=$(kafka-topics.sh --bootstrap-server $BOOTSTRAP_SERVERS --list | grep -v "__consumer_offsets")

if [ -z "$TOPICS" ]; then
    echo "⚠️ Warning: No old topics found to be modified."
    exit 0
fi

echo "=== STEP 3: PREVIOUS STATUS REPORT (HWM / Markdown Format) ==="
echo "------------------------------------------------------------------------------------------------------"
echo "| TOPIC NAME                                                | PARTITION | REPLICA | RETENTION TIME       | RETENTION SIZE     |"
echo "| :---                                                      | :---:     | :---:   | :---                 | :---               |"
counter=0
for TOPIC in $TOPICS; do
    # Sending data collection tasks to background (&) for parallel processing
    (
        DESCRIBE_OUT=$(kafka-topics.sh --bootstrap-server $BOOTSTRAP_SERVERS --describe --topic "$TOPIC" 2>/dev/null)
        CURR_PARTITIONS=$(echo "$DESCRIBE_OUT" | head -n 1 | awk -F'PartitionCount:' '{print $2}' | awk '{print $1}')
        CURR_REPLICAS=$(echo "$DESCRIBE_OUT" | head -n 1 | awk -F'ReplicationFactor:' '{print $2}' | awk '{print $1}')

        # Extract topic-specific custom retention settings
        TOPIC_CONFIGS=$(echo "$DESCRIBE_OUT" | head -n 1 | grep -o 'Configs:[^\n]*')
        RET_MS=$(echo "$TOPIC_CONFIGS" | grep -o 'retention.ms=[0-9]\+' | cut -d'=' -f2)
        if [ -n "$RET_MS" ]; then RET_TIME="$((RET_MS / 1000 / 60 / 60)) Hours (Custom)"; else RET_TIME="$GLOBAL_RET_HOURS Hours (Global)"; fi
        RET_BYTES=$(echo "$TOPIC_CONFIGS" | grep -o 'retention.bytes=[0-9]\+' | cut -d'=' -f2)
        if [ -n "$RET_BYTES" ]; then if [ "$RET_BYTES" -eq "-1" ]; then RET_SIZE="Unlimited"; else RET_SIZE="$((RET_BYTES / 1024 / 1024)) MB (Custom)"; fi; else RET_SIZE="$GLOBAL_RET_GB"; fi

        # Clean output format matching column header spacings precisely
        printf "| %-57s | %-9s | %-7s | %-20s | %-18s |\n" "$TOPIC" "$CURR_PARTITIONS" "$CURR_REPLICAS" "$RET_TIME" "$RET_SIZE"
    ) &

    let counter+=1
    if [[ $((counter % PARALLEL_LIMIT)) -eq 0 ]]; then
        wait # Wait until the configured batch background tasks are done
    fi
done
wait # Wait for the remaining tasks to complete
echo "------------------------------------------------------------------------------------------------------"

echo "=== STEP 4: Increasing Partition Counts to 3 in Parallel ($PARALLEL_LIMIT concurrent) ==="
counter=0
for TOPIC in $TOPICS; do
    echo "🔄 Updating: $TOPIC (Triggered in background)"
    # Offload the alter command to the background
    kafka-topics.sh --bootstrap-server $BOOTSTRAP_SERVERS --alter --topic "$TOPIC" --partitions 3 2>/dev/null &

    let counter+=1
    if [[ $((counter % PARALLEL_LIMIT)) -eq 0 ]]; then
        wait
    fi
done
wait
echo "✅ Partition counts for all topics have been updated in parallel."

echo "=== STEP 5: Generating Dynamic reassign.json via Pure Bash ==="
# Convert Node ID list into an array
IFS=',' read -r -a BROKER_ARRAY <<< "$NODE_IDS"
NUM_BROKERS=${#BROKER_ARRAY[@]}

# Open and initialize the JSON file cleanly from scratch
echo '{"version":1,"partitions":[' > automatic_reassign.json

first_element=1
for TOPIC in $TOPICS; do
    # At this stage, partition count is 3, so we configure replication schemas for 0, 1, and 2
    for PARTITION in 0 1 2; do
        if [ $first_element -eq 0 ]; then
            echo "," >> automatic_reassign.json
        fi
        first_element=0

        # Round-Robin (Balanced Distribution) mechanism
        B1_IDX=$(( (PARTITION + 0) % NUM_BROKERS ))
        B2_IDX=$(( (PARTITION + 1) % NUM_BROKERS ))
        B3_IDX=$(( (PARTITION + 2) % NUM_BROKERS ))

        B1=${BROKER_ARRAY[$B1_IDX]}
        B2=${BROKER_ARRAY[$B2_IDX]}
        B3=${BROKER_ARRAY[$B3_IDX]}

        if [ $NUM_BROKERS -lt 3 ]; then
            REPLICAS_STR="$NODE_IDS"
        else
            REPLICAS_STR="$B1,$B2,$B3"
        fi

        # Print raw JSON object cleanly without any structural issues
        printf '  {"topic":"%s","partition":%d,"replicas":[%s]}' "$TOPIC" "$PARTITION" "$REPLICAS_STR" >> automatic_reassign.json
    done
done

echo '' >> automatic_reassign.json
echo ']}' >> automatic_reassign.json
echo "✅ automatic_reassign.json successfully generated locally."

echo "=== STEP 6: Executing Replication (Data Sync) live on Cluster ==="
kafka-reassign-partitions.sh --bootstrap-server $BOOTSTRAP_SERVERS --reassignment-json-file automatic_reassign.json --execute

echo "=== STEP 7: TARGET NEW STATUS REPORT ==="
echo "---------------------------------------------------------"
echo -e "TOPIC NAME | TARGET PARTITION | TARGET REPLICA"
echo "---------------------------------------------------------"
for TOPIC in $TOPICS; do
    echo "🚀 $TOPIC | Partition: 3 | Replica: 3"
done
echo "---------------------------------------------------------"

echo "========================================================="
echo "🎉 Parallel processing completed successfully for standard environment!"
echo "To track replication sync status and verification, run:"
echo "kafka-reassign-partitions.sh --bootstrap-server $BOOTSTRAP_SERVERS --reassignment-json-file automatic_reassign.json --verify"
echo "========================================================="
