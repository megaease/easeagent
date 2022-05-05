# Benchmark
We use [spring-petclinic applications](https://github.com/spring-petclinic/spring-petclinic-microservices) as the benchmark application because it is relatively close to the real user scenario.

We use three AWS EC2 machines in a subnet for the benchmark test:

- Gateway EC2: deploy `api-gateway`, `config-service`, and `discovery-server` on this node.
- Service EC2: deploy `customers-service`, `vets-service`, and `visits-service` on this node.
- Tester EC2: we initiate testing requests and collect testing metric data on this node.

On Gateway EC2 node:
```
# lunch up without EaseAgent
env COMPOSER=gateway-compose EASEAGENT="" ./benchmark.sh start

# lunch up with EaseAgent
env COMPOSER=gateway-compose EASEAGENT="true" ./benchmark.sh start

```

On Service EC2 node:
```
# lunch up without EaseAgent
env COMPOSER=service-compose EASEAGENT="" ./benchmark.sh start

# lunch up with EaseAgent
env COMPOSER=service-compose EASEAGENT="true" ./benchmark.sh start

```

On Tester EC2 node, we start Grafana/Prometheus:
```
env COMPOSER=benchmark-tester-compose EASEAGENT="" PROMETHEUS_CONFIG_FILE=prometheus_benchmark_2.yaml ./benchmark.sh start
```

We run `k6` on `Tester` node to start stress test, and collect the metric data.
```
k6 run vets.js
```

Environment details of the test, reference to the document in [Easeagent-spring-petclinic](https://github.com/megaease/easeagent-spring-petclinic/blob/main/doc/benchmark.md).

## Test Result

We access the URI: `/api/vet/vets` of the application, it will generate 10-Spans in total, and distributed in two serivces:
- jmx-api-gateway: 1 span;
- jmx-vets: 9spans.

By comparing the results of the two services, the impact of the number of SPANs can be observed.

For each value pair in the table, the former is the baseline value and the latter is the agent value (baseline / agent / difference value).

| URI-Label                | CPU (baseline/agent/diff) | Heap Memory    | P90 Latency               | Throughput (15s) |
|----------------------|:--------------------------|:-------------------|:----------------------------------|-----------------------|
| jmx-api-gateway: 1spans | 57.5% / 61.1% / 3.6%   | 385M / 583M / 200M |                                   |                       |
| jmx-vets: 9spans        | 69.0% / 81.2% / 12.2%  | 303M / 464M / 151M | 564.69 ms / 737.55 ms / 30%       | 1800 / 1526 / -15.2%  |


- Process CPU Load: 3.6% - more than 10%, requiring optimisation, the number of span has a significant impact on CPU performance.
- Heap Memory: requiring optimisation.
- Latency & Throughput: Easeagent has an excessive impact on latency and throughput and is positively correlated with the number of Spans.

CPU, Process CPU Load, collected through JMX:java_lang_OperatingSystem_ProcessCpuLoad.

## Testing Data 
### CPU
- Baseline:
![Process CPU Load](./images/benchmark/baseline-process-cpu-load.png)

- Agent:
![Process CPU Load](./images/benchmark/agent-process-cpu-load.png)

### Heap
- Baseline:
![Heap](./images/benchmark/baseline-mem.png)
- Agent:
![Heap](./images/benchmark/agent-mem.png)

### Lagency & Throuthput
- Baseline:
```
running (13m20.6s), 000/700 VUs, 360354 complete and 0 interrupted iterations
default ↓ [======================================] 009/700 VUs  13m20s

     data_received..................: 1.1 GB  1.4 MB/s
     data_sent......................: 137 MB  171 kB/s
     http_req_blocked...............: avg=6.6µs    min=704ns  med=1.88µs   max=269.76ms p(90)=3.35µs   p(95)=5.16µs
     http_req_connecting............: avg=3.22µs   min=0s     med=0s       max=254.77ms p(90)=0s       p(95)=0s

     http_req_duration..............: avg=262.62ms min=1.19ms med=219.42ms max=1.58s    p(90)=564.69ms p(95)=669.43ms

       { expected_response:true }...: avg=262.62ms min=1.19ms med=219.42ms max=1.58s    p(90)=564.69ms p(95)=669.43ms
     http_req_failed................: 0.00%   ✓ 0           ✗ 1441416
     http_req_receiving.............: avg=7.48ms   min=8.94µs med=517.01µs max=480.89ms p(90)=17.64ms  p(95)=38.22ms
     http_req_sending...............: avg=105.42µs min=3.72µs med=8.55µs   max=236.91ms p(90)=20.64µs  p(95)=37.64µs
     http_req_tls_handshaking.......: avg=0s       min=0s     med=0s       max=0s       p(90)=0s       p(95)=0s
     http_req_waiting...............: avg=255.03ms min=1.12ms med=208.8ms  max=1.58s    p(90)=554.48ms p(95)=661.33ms

     http_reqs......................: 1441416 1800.434166/s

     iteration_duration.............: avg=1.37s    min=1s     med=1.36s    max=2.69s    p(90)=1.74s    p(95)=1.86s
     iterations.....................: 360354  450.108541/s
     vus............................: 9       min=5         max=700
     vus_max........................: 700     min=700       max=700

```

- Agent
```
running (13m20.9s), 000/700 VUs, 305670 complete and 0 interrupted iterations
default ✓ [======================================] 000/700 VUs  13m20s

     data_received..................: 956 MB  1.2 MB/s
     data_sent......................: 116 MB  145 kB/s
     http_req_blocked...............: avg=11.59µs  min=688ns  med=1.94µs   max=399.48ms p(90)=3.48µs   p(95)=4.62µs
     http_req_connecting............: avg=6.21µs   min=0s     med=0s       max=217.13ms p(90)=0s       p(95)=0s

     http_req_duration..............: avg=360.83ms min=1.45ms med=252.27ms max=7.99s    p(90)=737.55ms p(95)=1.04s

       { expected_response:true }...: avg=360.83ms min=1.45ms med=252.27ms max=7.99s    p(90)=737.55ms p(95)=1.04s
     http_req_failed................: 0.00%   ✓ 0           ✗ 1222680
     http_req_receiving.............: avg=4.88ms   min=9.14µs med=51.6µs   max=887.76ms p(90)=10.7ms   p(95)=28.04ms
     http_req_sending...............: avg=193.63µs min=3.72µs med=8.54µs   max=756.82ms p(90)=19.57µs  p(95)=44.94µs
     http_req_tls_handshaking.......: avg=0s       min=0s     med=0s       max=0s       p(90)=0s       p(95)=0s
     http_req_waiting...............: avg=355.75ms min=1.36ms med=248.41ms max=7.99s    p(90)=727.94ms p(95)=1.03s

     http_reqs......................: 1222680 1526.574647/s

     iteration_duration.............: avg=1.62s    min=1s     med=1.41s    max=8.99s    p(90)=2.36s    p(95)=2.76s
     iterations.....................: 305670  381.643662/s
     vus............................: 6       min=5         max=700
     vus_max........................: 700     min=700       max=700

```

## Conclusion

Optimisation is required in terms of cpu efficiency/memory usage and latency throughput.
