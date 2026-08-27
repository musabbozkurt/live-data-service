# Kafka Production Configuration Guide for 3-Node KRaft Cluster

In a production environment, default configurations like `replication-factor=1` and `num.partitions=1` **must never be
used**. Leaving these default values causes **severe data loss** if a broker fails and completely chokes consumer
performance by preventing parallel processing.

To prevent data loss and maximize throughput, apply the following production-ready configuration changes to your
**3-Node KRaft Cluster** architecture.

---

## 1. Broker & Topic Level Configurations

Update these properties within your cluster settings (`server.properties` or Docker environment variables) to ensure
that all newly auto-created topics inherit proper performance and resiliency attributes:

* **`num.partitions` ➜ Set to at least 3 (or more):**
    * *Why?* Kafka consumer scalability is bound to the partition count. A topic with only '1' partition can only be
      read by a single consumer thread at any given time. Increasing this to 3 allows multiple consumer instances or
      threads to ingest data concurrently.
* **`offsets.topic.num.partitions` ➜ 50 (Keep default):**
    * This regulates the internal system topic (`__consumer_offsets`) which tracks consumer offsets. Do not downgrade
      this to 1.
* **`offsets.topic.replication.factor` ➜ 3:**
    * Ensures consumer position history is distributed safely across all three nodes.
* **`default.replication.factor` ➜ 3:**
    * Ensures any topic auto-created by Spring Boot immediately provisions '3' distinct replicas. If up to '2' brokers
      crash, your pipeline continues running with zero data loss.

---

## 2. Producer Properties for Zero Data Loss

Setting the replication factor to 3 is not enough on its own; the client application must explicitly request
acknowledgment from these replicas. Configure your Spring Boot `application.yml` or client properties as follows:

```properties
# Guarantees the message is written to the leader and all active in-sync replicas (ISR) before acknowledging success.
acks=all
# Enables transient network error retries up to the maximum integer value to prevent data drop.
retries=2147483647
# Limits the number of unacknowledged requests in flight to preserve exact message ordering.
max.in.flight.requests.per.connection=5
# Activates strict idempotence to prevent duplicate message writes on network flickers or broker leader elections.
enable.idempotence=true
```

---

## 3. Broker Resiliency Settings (`min.insync.replicas`)

The `acks=all` mandate relies entirely on the cluster's acknowledgment threshold. This must be set at the broker level:

```properties
# Add to your server.properties or Topic configurations
min.insync.replicas=2
```

* **How it Works:** With `replication-factor=3` and `min.insync.replicas=2`, if '1' broker goes offline, the remaining
  '2' nodes continue accepting writes securely. If '2' brokers crash concurrently, the producer safely returns errors
  out instead of risking un-replicated data writes.

---

## 📊 Summary Table: Default Settings vs. Production Configuration

| Parameter / Property      | Default Value | Recommended (Production) | Key Performance & Safety Benefit                                            |
|:--------------------------|:--------------|:-------------------------|:----------------------------------------------------------------------------|
| **`num.partitions`**      | `1`           | **`3` or more**          | Unlocks parallel data processing and scales consumer pool throughput.       |
| **`replication-factor`**  | `1`           | **`3`**                  | Guarantees hardware-level data redundancy across the infrastructure.        |
| **`min.insync.replicas`** | `1`           | **`2`**                  | Enforces the minimum node handshake count required to accept incoming data. |
| **`acks`**                | `1`           | **`all` (or `-1`)**      | Triggers end-to-end data safety protocols from the application layer.       |

---

## 🛠️ KRaft Cluster Level Modifications (`server.properties`)

These detailed cluster properties must be adjusted on every broker configuration inside your 3-node KRaft topology:

| Feature / Parameter                        | Default Value | Recommended Value | Advantage & KRaft Core Impact                                                                                                                  |
|:-------------------------------------------|:--------------|:------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------|
| `num.partitions`                           | `1`           | **`3` (or `6`)**  | **Consumer Performance:** Unlocks synchronous processing for Spring consumers. With 1 partition, 2 out of 3 consumer instances would sit idle. |
| `default.replication.factor`               | `1`           | **`3`**           | **Zero Data Loss:** Automatically generates 3 mirrors when Spring emits a message to a new topic. Cluster survives a 2-node failure.           |
| `min.insync.replicas`                      | `1`           | **`2`**           | **Write Assurance:** Forces data to commit to at least 2 nodes. Offers the optimal balance of network throughput and safety for 3 nodes.       |
| `offsets.topic.replication.factor`         | `1` (or `3`)  | **`3`**           | **Cluster Stability:** Redundantly backs up the internal consumer position storage ledger. Essential to protect KRaft node failovers.          |
| `transaction.state.log.replication.factor` | `1`           | **`3`**           | **Data Consistency:** Prevents transactional index records from disappearing if you make use of `@Transactional` Kafka logic in Spring.        |
| `transaction.state.log.min.isr`            | `1`           | **`2`**           | **Write Assurance:** Demands a physical handshake from at least 2 nodes before certifying transaction boundary data.                           |

---

## 🟢 Spring Boot / Producer Level Modifications (`application.yml`)

To unlock the maximum potential and speed of your backend data ingestion pipelines, augment your Spring Boot client
properties with these variables:

| Feature / Parameter                                   | Default Value            | Recommended Value            | Practical Integration Advantage                                                                                                                                |
|:------------------------------------------------------|:-------------------------|:-----------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spring.kafka.producer.acks`                          | `1`                      | **`all` (or `-1`)**          | Triggers the cluster's `min.insync.replicas=2` threshold rule. The producer blocks until at least 2 nodes confirm write success, shielding against data drops. |
| `spring.kafka.producer.properties.enable.idempotence` | `true` (modern versions) | **`true`**                   | Strips out the danger of duplicate record streaming during network drops, connection timeouts, or sudden KRaft partition leader swaps.                         |
| `spring.kafka.listener.concurrency`                   | `1`                      | **`3` (Matches partitions)** | Spins up 3 concurrent, decoupled worker threads inside your JVM to pull messages simultaneously from the 3 partition streams at warp speed.                    |

---

## ⚠️ Critical Warning: Handling Existing Topics in Production

The cluster-wide variable shifts declared above **only apply to freshly initialized auto-created topics**. Any existing
legacy topic residing on disk with `1 partition` and `1 replica` will remain un-replicated and single-threaded.

To safely scale your live legacy production data structures without dropping events, run these execution routines
directly on your cluster management endpoints:

### Step 1: Scale Partition Layout (Boosts Consumer Multi-threading)

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --alter --topic YOUR_LEGACY_TOPIC_NAME --partitions 3
```

### Step 2: Scale Replication Topology (Secures Data Mirroring)

To elevate your reflection arrays from '1' copy up to '3' copies, you must create a migration mapping schema file named
`reassign.json` to safely back up partition sectors to your secondary cluster coordinates:

```bash
kafka-reassign-partitions.sh --bootstrap-server localhost:9092 --reassignment-json-file reassign.json --execute
```

---

## 🚀 Automation: Scaling Existing Topics via Parallel Bash Scripts

To automate the migration of existing topics from `1 partition / 1 replica` to `3 partitions / 3 replicas`, you can use
the two custom parallel migration scripts provided below.

Both scripts automatically discover your active KRaft node IDs, query existing configurations concurrently using
background jobs, and balance replicas across available nodes using a Round-Robin strategy.

### 📋 Prerequisites & Planning Before Execution

1. Ensure your cluster configurations (`server.properties` or environment variables) are updated and brokers have
   undergone a **rolling restart** so new topics are safely created with '3' replicas.
2. The migration scripts run **entirely online** while production operations are ongoing.
3. Review your **`PARALLEL_LIMIT`** variable inside the scripts based on your topic count and infrastructure capacity:
    * `1`: Sequential processing (Slowest, gentlest on resources).
    * `5` to `10`: Moderate batching (Recommended balanced execution profile).
    * `30+`: High concurrency execution (Fastest, requires adequate CPU and network allocation).

---

### 🐳 Option A: Running from Host OS for Docker Configurations

Use this script if your Kafka cluster runs inside Docker containers, but you want to execute and track the entire update
process **directly from your host machine** without attaching to containers.

#### Setup & Execution Steps:

1. Create a script file on your host machine:
   ```bash
   nano kafka-upgrade-docker-parallel.sh
   ```
2. Paste the script content (configured with your target `PARALLEL_LIMIT`, e.g., `30`).
3. Save the file and grand execution permissions:
   ```bash
   chmod +x kafka-upgrade-docker-parallel.sh
   ```
4. Run the script:
   ```bash
   ./kafka-upgrade-docker-parallel.sh
   ```

#### How it works under the hood:

* Automatically targets and talks to your active Kafka container (`broker` or `kafka`) using optimized `docker exec`
  hooks.
* Leverages native Bash multitasking pipelines (`&` and `wait`) to query and alter topic schemas in concurrent
  micro-batches.
* Constructs a syntactically correct layout object named `automatic_reassign.json` locally and pushes it to your
  container via `docker cp` to spin up background partitions data-balancing operations.

---

### 💻 Option B: Running on Bare-Metal Linux Systems (Standard Setup)

Use this script if Kafka is installed directly on your physical or virtual Linux servers, or if you prefer running
scripts **inside** the container context.

#### Setup & Execution Steps:

1. Create a script file on your server or attached environment terminal:
   ```bash
   nano kafka-upgrade-standard-parallel.sh
   ```
2. Paste the native standalone script content.
3. Save the file and grant execution permissions:
   ```bash
   chmod +x kafka-upgrade-standard-parallel.sh
   ```
4. Run the script:
   ```bash
   ./kafka-upgrade-standard-parallel.sh
   ```

#### How it works under the hood:

* Reference your active server environment using local Kafka command binaries (`kafka-topics.sh`,
  `kafka-reassign-partitions.sh`).
* Safely generates an `automatic_reassign.json` migration log block directly inside your current directory (`pwd`) using
  highly reliable `printf` layout constraints.

---

### 🔍 Monitoring Progress & Verifying Success

Once Step 8 (Docker) or Step 6 (Standard) fires, Kafka will begin mirroring existing data payloads across nodes
asynchronously in the background. Your console will instantly provide a customized path statement.

Run the tracking wrapper provided at the end of the script execution string to verify partition health:

* **For Docker Host Setups:**
  ```bash
  docker exec -it <YOUR_CONTAINER_NAME> kafka-reassign-partitions --bootstrap-server localhost:9092 --reassignment-json-file /tmp/automatic_reassign.json --verify
  ```
* **For Standard Setups:**
  ```bash
  kafka-reassign-partitions.sh --bootstrap-server localhost:9092 --reassignment-json-file automatic_reassign.json --verify
  ```

Look for the status indicator string **`Status: Successfully completed`** next to each tracked sector block. This
confirms your system has reached an optimized, resilient multithreaded production state.
