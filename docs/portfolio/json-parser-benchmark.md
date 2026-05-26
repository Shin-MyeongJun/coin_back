요약: tickMessageDeserialize throughput 기준 Jackson 1.00x / Jsoniter 1.58x / DSL-JSON 1.46x (모드: throughput, JDK 21, Windows 11, amd64, AMD64 Family 25 Model 97 Stepping 2, AuthenticAMD)

# JSON Parser Benchmark

- 결과 파일: `modules/benchmarks/build/reports/jmh/results.json`
- GC 프로파일러: `gc.alloc.rate.norm` 기준 B/op
- Jackson 대비 배수: throughput은 `parser / Jackson`, average time은 `Jackson / parser`
- 측정 조건: forks=2, warmupIterations=5, warmupTime=1 s, measurementIterations=5, measurementTime=1 s

## binanceBookTickerDeserialize

| Parser | Mode | Score (ops/us) | Error | Allocation (B/op) | Jackson 대비 배수 |
| --- | --- | ---: | ---: | ---: | ---: |
| DSL-JSON | average time | 0.438909 | 0.010941 | 2120.003 | 1.37x |
| Jackson | average time | 0.600148 | 0.014223 | 1568.004 | 1.00x |
| Jsoniter | average time | 0.396333 | 0.008673 | 1240.003 | 1.51x |
| DSL-JSON | throughput | 2.314 | 0.035781 | 2120.003 | 1.49x |
| Jackson | throughput | 1.55 | 0.1179 | 1568.004 | 1.00x |
| Jsoniter | throughput | 2.513 | 0.070996 | 1240.003 | 1.62x |

## tickMessageBatch1000Deserialize

| Parser | Mode | Score (ops/us) | Error | Allocation (B/op) | Jackson 대비 배수 |
| --- | --- | ---: | ---: | ---: | ---: |
| DSL-JSON | average time | 202.434 | 5.665 | 1664001.397 | 1.44x |
| Jackson | average time | 290.971 | 21.665 | 848002.003 | 1.00x |
| Jsoniter | average time | 186.792 | 4.783 | 512001.29 | 1.56x |
| DSL-JSON | throughput | 0.004997 | 0.000076487 | 1664001.383 | 1.42x |
| Jackson | throughput | 0.00352 | 0.000054928 | 848001.959 | 1.00x |
| Jsoniter | throughput | 0.005282 | 0.000280522 | 512001.301 | 1.50x |

## tickMessageDeserialize

| Parser | Mode | Score (ops/us) | Error | Allocation (B/op) | Jackson 대비 배수 |
| --- | --- | ---: | ---: | ---: | ---: |
| DSL-JSON | average time | 0.199936 | 0.004323 | 1696.001 | 1.44x |
| Jackson | average time | 0.286912 | 0.007931 | 848.002 | 1.00x |
| Jsoniter | average time | 0.182107 | 0.001326 | 512.001 | 1.58x |
| DSL-JSON | throughput | 5.044 | 0.096189 | 1696.001 | 1.46x |
| Jackson | throughput | 3.465 | 0.090453 | 848.002 | 1.00x |
| Jsoniter | throughput | 5.475 | 0.097782 | 512.001 | 1.58x |

## tickMessageSerialize

| Parser | Mode | Score (ops/us) | Error | Allocation (B/op) | Jackson 대비 배수 |
| --- | --- | ---: | ---: | ---: | ---: |
| DSL-JSON | average time | 0.069307 | 0.001644 | 328 | 1.86x |
| Jackson | average time | 0.129134 | 0.001538 | 576.001 | 1.00x |
| Jsoniter | average time | 0.133637 | 0.001516 | 496.001 | 0.97x |
| DSL-JSON | throughput | 14.335 | 0.24095 | 328 | 1.84x |
| Jackson | throughput | 7.778 | 0.091798 | 576.001 | 1.00x |
| Jsoniter | throughput | 7.509 | 0.083415 | 496.001 | 0.97x |

## upbitOrderbookDeserialize

| Parser | Mode | Score (ops/us) | Error | Allocation (B/op) | Jackson 대비 배수 |
| --- | --- | ---: | ---: | ---: | ---: |
| DSL-JSON | average time | 0.84004 | 0.013164 | 2744.006 | 1.36x |
| Jackson | average time | 1.14 | 0.025637 | 2040.008 | 1.00x |
| Jsoniter | average time | 0.914453 | 0.075393 | 2608.006 | 1.25x |
| DSL-JSON | throughput | 1.188 | 0.014632 | 2744.006 | 1.31x |
| Jackson | throughput | 0.908052 | 0.012402 | 2040.008 | 1.00x |
| Jsoniter | throughput | 1.132 | 0.044179 | 2608.006 | 1.25x |

