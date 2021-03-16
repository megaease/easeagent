package com.megaease.easeagent.sniffer.lettuce.v5.advice;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.output.*;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.ProtocolKeyword;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MyBaseRedisCommands<K,V> implements RedisCommands<K,V> {
    @Override
    public void setTimeout(Duration timeout) {

    }

    @Override
    public void setTimeout(long timeout, TimeUnit unit) {

    }

    @Override
    public String auth(String password) {
        return null;
    }

    @Override
    public String clusterBumpepoch() {
        return null;
    }

    @Override
    public String clusterMeet(String ip, int port) {
        return null;
    }

    @Override
    public String clusterForget(String nodeId) {
        return null;
    }

    @Override
    public String clusterAddSlots(int... slots) {
        return null;
    }

    @Override
    public String clusterDelSlots(int... slots) {
        return null;
    }

    @Override
    public String clusterSetSlotNode(int slot, String nodeId) {
        return null;
    }

    @Override
    public String clusterSetSlotStable(int slot) {
        return null;
    }

    @Override
    public String clusterSetSlotMigrating(int slot, String nodeId) {
        return null;
    }

    @Override
    public String clusterSetSlotImporting(int slot, String nodeId) {
        return null;
    }

    @Override
    public String clusterInfo() {
        return null;
    }

    @Override
    public String clusterMyId() {
        return null;
    }

    @Override
    public String clusterNodes() {
        return null;
    }

    @Override
    public List<String> clusterSlaves(String nodeId) {
        return null;
    }

    @Override
    public List<K> clusterGetKeysInSlot(int slot, int count) {
        return null;
    }

    @Override
    public Long clusterCountKeysInSlot(int slot) {
        return null;
    }

    @Override
    public Long clusterCountFailureReports(String nodeId) {
        return null;
    }

    @Override
    public Long clusterKeyslot(K key) {
        return null;
    }

    @Override
    public String clusterSaveconfig() {
        return null;
    }

    @Override
    public String clusterSetConfigEpoch(long configEpoch) {
        return null;
    }

    @Override
    public List<Object> clusterSlots() {
        return null;
    }

    @Override
    public String asking() {
        return null;
    }

    @Override
    public String clusterReplicate(String nodeId) {
        return null;
    }

    @Override
    public String clusterFailover(boolean force) {
        return null;
    }

    @Override
    public String clusterReset(boolean hard) {
        return null;
    }

    @Override
    public String clusterFlushslots() {
        return null;
    }

    @Override
    public String select(int db) {
        return null;
    }

    @Override
    public String swapdb(int db1, int db2) {
        return null;
    }

    @Override
    public StatefulRedisConnection<K, V> getStatefulConnection() {
        return null;
    }

    @Override
    public Long publish(K channel, V message) {
        return null;
    }

    @Override
    public List<K> pubsubChannels() {
        return null;
    }

    @Override
    public List<K> pubsubChannels(K channel) {
        return null;
    }

    @Override
    public Map<K, Long> pubsubNumsub(K... channels) {
        return null;
    }

    @Override
    public Long pubsubNumpat() {
        return null;
    }

    @Override
    public V echo(V msg) {
        return null;
    }

    @Override
    public List<Object> role() {
        return null;
    }

    @Override
    public String ping() {
        return null;
    }

    @Override
    public String readOnly() {
        return null;
    }

    @Override
    public String readWrite() {
        return null;
    }

    @Override
    public String quit() {
        return null;
    }

    @Override
    public Long waitForReplication(int replicas, long timeout) {
        return null;
    }

    @Override
    public <T> T dispatch(ProtocolKeyword type, CommandOutput<K, V, T> output) {
        return null;
    }

    @Override
    public <T> T dispatch(ProtocolKeyword type, CommandOutput<K, V, T> output, CommandArgs<K, V> args) {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public Long geoadd(K key, double longitude, double latitude, V member) {
        return null;
    }

    @Override
    public Long geoadd(K key, Object... lngLatMember) {
        return null;
    }

    @Override
    public List<Value<String>> geohash(K key, V... members) {
        return null;
    }

    @Override
    public Set<V> georadius(K key, double longitude, double latitude, double distance, GeoArgs.Unit unit) {
        return null;
    }

    @Override
    public List<GeoWithin<V>> georadius(K key, double longitude, double latitude, double distance, GeoArgs.Unit unit, GeoArgs geoArgs) {
        return null;
    }

    @Override
    public Long georadius(K key, double longitude, double latitude, double distance, GeoArgs.Unit unit, GeoRadiusStoreArgs<K> geoRadiusStoreArgs) {
        return null;
    }

    @Override
    public Set<V> georadiusbymember(K key, V member, double distance, GeoArgs.Unit unit) {
        return null;
    }

    @Override
    public List<GeoWithin<V>> georadiusbymember(K key, V member, double distance, GeoArgs.Unit unit, GeoArgs geoArgs) {
        return null;
    }

    @Override
    public Long georadiusbymember(K key, V member, double distance, GeoArgs.Unit unit, GeoRadiusStoreArgs<K> geoRadiusStoreArgs) {
        return null;
    }

    @Override
    public List<GeoCoordinates> geopos(K key, V... members) {
        return null;
    }

    @Override
    public Double geodist(K key, V from, V to, GeoArgs.Unit unit) {
        return null;
    }

    @Override
    public Long pfadd(K key, V... values) {
        return null;
    }

    @Override
    public String pfmerge(K destkey, K... sourcekeys) {
        return null;
    }

    @Override
    public Long pfcount(K... keys) {
        return null;
    }

    @Override
    public Long hdel(K key, K... fields) {
        return null;
    }

    @Override
    public Boolean hexists(K key, K field) {
        return null;
    }

    @Override
    public V hget(K key, K field) {
        return null;
    }

    @Override
    public Long hincrby(K key, K field, long amount) {
        return null;
    }

    @Override
    public Double hincrbyfloat(K key, K field, double amount) {
        return null;
    }

    @Override
    public Map<K, V> hgetall(K key) {
        return null;
    }

    @Override
    public Long hgetall(KeyValueStreamingChannel<K, V> channel, K key) {
        return null;
    }

    @Override
    public List<K> hkeys(K key) {
        return null;
    }

    @Override
    public Long hkeys(KeyStreamingChannel<K> channel, K key) {
        return null;
    }

    @Override
    public Long hlen(K key) {
        return null;
    }

    @Override
    public List<KeyValue<K, V>> hmget(K key, K... fields) {
        return null;
    }

    @Override
    public Long hmget(KeyValueStreamingChannel<K, V> channel, K key, K... fields) {
        return null;
    }

    @Override
    public String hmset(K key, Map<K, V> map) {
        return null;
    }

    @Override
    public MapScanCursor<K, V> hscan(K key) {
        return null;
    }

    @Override
    public MapScanCursor<K, V> hscan(K key, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public MapScanCursor<K, V> hscan(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public MapScanCursor<K, V> hscan(K key, ScanCursor scanCursor) {
        return null;
    }

    @Override
    public StreamScanCursor hscan(KeyValueStreamingChannel<K, V> channel, K key) {
        return null;
    }

    @Override
    public StreamScanCursor hscan(KeyValueStreamingChannel<K, V> channel, K key, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public StreamScanCursor hscan(KeyValueStreamingChannel<K, V> channel, K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public StreamScanCursor hscan(KeyValueStreamingChannel<K, V> channel, K key, ScanCursor scanCursor) {
        return null;
    }

    @Override
    public Boolean hset(K key, K field, V value) {
        return null;
    }

    @Override
    public Long hset(K key, Map<K, V> map) {
        return null;
    }

    @Override
    public Boolean hsetnx(K key, K field, V value) {
        return null;
    }

    @Override
    public Long hstrlen(K key, K field) {
        return null;
    }

    @Override
    public List<V> hvals(K key) {
        return null;
    }

    @Override
    public Long hvals(ValueStreamingChannel<V> channel, K key) {
        return null;
    }

    @Override
    public Long del(K... keys) {
        return null;
    }

    @Override
    public Long unlink(K... keys) {
        return null;
    }

    @Override
    public byte[] dump(K key) {
        return new byte[0];
    }

    @Override
    public Long exists(K... keys) {
        return null;
    }

    @Override
    public Boolean expire(K key, long seconds) {
        return null;
    }

    @Override
    public Boolean expireat(K key, Date timestamp) {
        return null;
    }

    @Override
    public Boolean expireat(K key, long timestamp) {
        return null;
    }

    @Override
    public List<K> keys(K pattern) {
        return null;
    }

    @Override
    public Long keys(KeyStreamingChannel<K> channel, K pattern) {
        return null;
    }

    @Override
    public String migrate(String host, int port, K key, int db, long timeout) {
        return null;
    }

    @Override
    public String migrate(String host, int port, int db, long timeout, MigrateArgs<K> migrateArgs) {
        return null;
    }

    @Override
    public Boolean move(K key, int db) {
        return null;
    }

    @Override
    public String objectEncoding(K key) {
        return null;
    }

    @Override
    public Long objectIdletime(K key) {
        return null;
    }

    @Override
    public Long objectRefcount(K key) {
        return null;
    }

    @Override
    public Boolean persist(K key) {
        return null;
    }

    @Override
    public Boolean pexpire(K key, long milliseconds) {
        return null;
    }

    @Override
    public Boolean pexpireat(K key, Date timestamp) {
        return null;
    }

    @Override
    public Boolean pexpireat(K key, long timestamp) {
        return null;
    }

    @Override
    public Long pttl(K key) {
        return null;
    }

    @Override
    public K randomkey() {
        return null;
    }

    @Override
    public String rename(K key, K newKey) {
        return null;
    }

    @Override
    public Boolean renamenx(K key, K newKey) {
        return null;
    }

    @Override
    public String restore(K key, long ttl, byte[] value) {
        return null;
    }

    @Override
    public String restore(K key, byte[] value, RestoreArgs args) {
        return null;
    }

    @Override
    public List<V> sort(K key) {
        return null;
    }

    @Override
    public Long sort(ValueStreamingChannel<V> channel, K key) {
        return null;
    }

    @Override
    public List<V> sort(K key, SortArgs sortArgs) {
        return null;
    }

    @Override
    public Long sort(ValueStreamingChannel<V> channel, K key, SortArgs sortArgs) {
        return null;
    }

    @Override
    public Long sortStore(K key, SortArgs sortArgs, K destination) {
        return null;
    }

    @Override
    public Long touch(K... keys) {
        return null;
    }

    @Override
    public Long ttl(K key) {
        return null;
    }

    @Override
    public String type(K key) {
        return null;
    }

    @Override
    public KeyScanCursor<K> scan() {
        return null;
    }

    @Override
    public KeyScanCursor<K> scan(ScanArgs scanArgs) {
        return null;
    }

    @Override
    public KeyScanCursor<K> scan(ScanCursor scanCursor, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public KeyScanCursor<K> scan(ScanCursor scanCursor) {
        return null;
    }

    @Override
    public StreamScanCursor scan(KeyStreamingChannel<K> channel) {
        return null;
    }

    @Override
    public StreamScanCursor scan(KeyStreamingChannel<K> channel, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public StreamScanCursor scan(KeyStreamingChannel<K> channel, ScanCursor scanCursor, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public StreamScanCursor scan(KeyStreamingChannel<K> channel, ScanCursor scanCursor) {
        return null;
    }

    @Override
    public KeyValue<K, V> blpop(long timeout, K... keys) {
        return null;
    }

    @Override
    public KeyValue<K, V> brpop(long timeout, K... keys) {
        return null;
    }

    @Override
    public V brpoplpush(long timeout, K source, K destination) {
        return null;
    }

    @Override
    public V lindex(K key, long index) {
        return null;
    }

    @Override
    public Long linsert(K key, boolean before, V pivot, V value) {
        return null;
    }

    @Override
    public Long llen(K key) {
        return null;
    }

    @Override
    public V lpop(K key) {
        return null;
    }

    @Override
    public Long lpos(K key, V value) {
        return null;
    }

    @Override
    public Long lpos(K key, V value, LPosArgs args) {
        return null;
    }

    @Override
    public List<Long> lpos(K key, V value, int count) {
        return null;
    }

    @Override
    public List<Long> lpos(K key, V value, int count, LPosArgs args) {
        return null;
    }

    @Override
    public Long lpush(K key, V... values) {
        return null;
    }

    @Override
    public Long lpushx(K key, V... values) {
        return null;
    }

    @Override
    public List<V> lrange(K key, long start, long stop) {
        return null;
    }

    @Override
    public Long lrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        return null;
    }

    @Override
    public Long lrem(K key, long count, V value) {
        return null;
    }

    @Override
    public String lset(K key, long index, V value) {
        return null;
    }

    @Override
    public String ltrim(K key, long start, long stop) {
        return null;
    }

    @Override
    public V rpop(K key) {
        return null;
    }

    @Override
    public V rpoplpush(K source, K destination) {
        return null;
    }

    @Override
    public Long rpush(K key, V... values) {
        return null;
    }

    @Override
    public Long rpushx(K key, V... values) {
        return null;
    }

    @Override
    public <T> T eval(String script, ScriptOutputType type, K... keys) {
        return null;
    }

    @Override
    public <T> T eval(String script, ScriptOutputType type, K[] keys, V... values) {
        return null;
    }

    @Override
    public <T> T evalsha(String digest, ScriptOutputType type, K... keys) {
        return null;
    }

    @Override
    public <T> T evalsha(String digest, ScriptOutputType type, K[] keys, V... values) {
        return null;
    }

    @Override
    public List<Boolean> scriptExists(String... digests) {
        return null;
    }

    @Override
    public String scriptFlush() {
        return null;
    }

    @Override
    public String scriptKill() {
        return null;
    }

    @Override
    public String scriptLoad(V script) {
        return null;
    }

    @Override
    public String digest(V script) {
        return null;
    }

    @Override
    public String bgrewriteaof() {
        return null;
    }

    @Override
    public String bgsave() {
        return null;
    }

    @Override
    public K clientGetname() {
        return null;
    }

    @Override
    public String clientSetname(K name) {
        return null;
    }

    @Override
    public String clientKill(String addr) {
        return null;
    }

    @Override
    public Long clientKill(KillArgs killArgs) {
        return null;
    }

    @Override
    public Long clientUnblock(long id, UnblockType type) {
        return null;
    }

    @Override
    public String clientPause(long timeout) {
        return null;
    }

    @Override
    public String clientList() {
        return null;
    }

    @Override
    public Long clientId() {
        return null;
    }

    @Override
    public List<Object> command() {
        return null;
    }

    @Override
    public List<Object> commandInfo(String... commands) {
        return null;
    }

    @Override
    public List<Object> commandInfo(CommandType... commands) {
        return null;
    }

    @Override
    public Long commandCount() {
        return null;
    }

    @Override
    public Map<String, String> configGet(String parameter) {
        return null;
    }

    @Override
    public String configResetstat() {
        return null;
    }

    @Override
    public String configRewrite() {
        return null;
    }

    @Override
    public String configSet(String parameter, String value) {
        return null;
    }

    @Override
    public Long dbsize() {
        return null;
    }

    @Override
    public String debugCrashAndRecover(Long delay) {
        return null;
    }

    @Override
    public String debugHtstats(int db) {
        return null;
    }

    @Override
    public String debugObject(K key) {
        return null;
    }

    @Override
    public void debugOom() {

    }

    @Override
    public void debugSegfault() {

    }

    @Override
    public String debugReload() {
        return null;
    }

    @Override
    public String debugRestart(Long delay) {
        return null;
    }

    @Override
    public String debugSdslen(K key) {
        return null;
    }

    @Override
    public String flushall() {
        return null;
    }

    @Override
    public String flushallAsync() {
        return null;
    }

    @Override
    public String flushdb() {
        return null;
    }

    @Override
    public String flushdbAsync() {
        return null;
    }

    @Override
    public String info() {
        return null;
    }

    @Override
    public String info(String section) {
        return null;
    }

    @Override
    public Date lastsave() {
        return null;
    }

    @Override
    public Long memoryUsage(K key) {
        return null;
    }

    @Override
    public String save() {
        return null;
    }

    @Override
    public void shutdown(boolean save) {

    }

    @Override
    public String slaveof(String host, int port) {
        return null;
    }

    @Override
    public String slaveofNoOne() {
        return null;
    }

    @Override
    public List<Object> slowlogGet() {
        return null;
    }

    @Override
    public List<Object> slowlogGet(int count) {
        return null;
    }

    @Override
    public Long slowlogLen() {
        return null;
    }

    @Override
    public String slowlogReset() {
        return null;
    }

    @Override
    public List<V> time() {
        return null;
    }

    @Override
    public Long sadd(K key, V... members) {
        return null;
    }

    @Override
    public Long scard(K key) {
        return null;
    }

    @Override
    public Set<V> sdiff(K... keys) {
        return null;
    }

    @Override
    public Long sdiff(ValueStreamingChannel<V> channel, K... keys) {
        return null;
    }

    @Override
    public Long sdiffstore(K destination, K... keys) {
        return null;
    }

    @Override
    public Set<V> sinter(K... keys) {
        return null;
    }

    @Override
    public Long sinter(ValueStreamingChannel<V> channel, K... keys) {
        return null;
    }

    @Override
    public Long sinterstore(K destination, K... keys) {
        return null;
    }

    @Override
    public Boolean sismember(K key, V member) {
        return null;
    }

    @Override
    public Boolean smove(K source, K destination, V member) {
        return null;
    }

    @Override
    public Set<V> smembers(K key) {
        return null;
    }

    @Override
    public Long smembers(ValueStreamingChannel<V> channel, K key) {
        return null;
    }

    @Override
    public V spop(K key) {
        return null;
    }

    @Override
    public Set<V> spop(K key, long count) {
        return null;
    }

    @Override
    public V srandmember(K key) {
        return null;
    }

    @Override
    public List<V> srandmember(K key, long count) {
        return null;
    }

    @Override
    public Long srandmember(ValueStreamingChannel<V> channel, K key, long count) {
        return null;
    }

    @Override
    public Long srem(K key, V... members) {
        return null;
    }

    @Override
    public Set<V> sunion(K... keys) {
        return null;
    }

    @Override
    public Long sunion(ValueStreamingChannel<V> channel, K... keys) {
        return null;
    }

    @Override
    public Long sunionstore(K destination, K... keys) {
        return null;
    }

    @Override
    public ValueScanCursor<V> sscan(K key) {
        return null;
    }

    @Override
    public ValueScanCursor<V> sscan(K key, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public ValueScanCursor<V> sscan(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public ValueScanCursor<V> sscan(K key, ScanCursor scanCursor) {
        return null;
    }

    @Override
    public StreamScanCursor sscan(ValueStreamingChannel<V> channel, K key) {
        return null;
    }

    @Override
    public StreamScanCursor sscan(ValueStreamingChannel<V> channel, K key, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public StreamScanCursor sscan(ValueStreamingChannel<V> channel, K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public StreamScanCursor sscan(ValueStreamingChannel<V> channel, K key, ScanCursor scanCursor) {
        return null;
    }

    @Override
    public KeyValue<K, ScoredValue<V>> bzpopmin(long timeout, K... keys) {
        return null;
    }

    @Override
    public KeyValue<K, ScoredValue<V>> bzpopmax(long timeout, K... keys) {
        return null;
    }

    @Override
    public Long zadd(K key, double score, V member) {
        return null;
    }

    @Override
    public Long zadd(K key, Object... scoresAndValues) {
        return null;
    }

    @Override
    public Long zadd(K key, ScoredValue<V>... scoredValues) {
        return null;
    }

    @Override
    public Long zadd(K key, ZAddArgs zAddArgs, double score, V member) {
        return null;
    }

    @Override
    public Long zadd(K key, ZAddArgs zAddArgs, Object... scoresAndValues) {
        return null;
    }

    @Override
    public Long zadd(K key, ZAddArgs zAddArgs, ScoredValue<V>... scoredValues) {
        return null;
    }

    @Override
    public Double zaddincr(K key, double score, V member) {
        return null;
    }

    @Override
    public Double zaddincr(K key, ZAddArgs zAddArgs, double score, V member) {
        return null;
    }

    @Override
    public Long zcard(K key) {
        return null;
    }

    @Override
    public Long zcount(K key, double min, double max) {
        return null;
    }

    @Override
    public Long zcount(K key, String min, String max) {
        return null;
    }

    @Override
    public Long zcount(K key, Range<? extends Number> range) {
        return null;
    }

    @Override
    public Double zincrby(K key, double amount, V member) {
        return null;
    }

    @Override
    public Long zinterstore(K destination, K... keys) {
        return null;
    }

    @Override
    public Long zinterstore(K destination, ZStoreArgs storeArgs, K... keys) {
        return null;
    }

    @Override
    public Long zlexcount(K key, String min, String max) {
        return null;
    }

    @Override
    public Long zlexcount(K key, Range<? extends V> range) {
        return null;
    }

    @Override
    public ScoredValue<V> zpopmin(K key) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zpopmin(K key, long count) {
        return null;
    }

    @Override
    public ScoredValue<V> zpopmax(K key) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zpopmax(K key, long count) {
        return null;
    }

    @Override
    public List<V> zrange(K key, long start, long stop) {
        return null;
    }

    @Override
    public Long zrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrangeWithScores(K key, long start, long stop) {
        return null;
    }

    @Override
    public Long zrangeWithScores(ScoredValueStreamingChannel<V> channel, K key, long start, long stop) {
        return null;
    }

    @Override
    public List<V> zrangebylex(K key, String min, String max) {
        return null;
    }

    @Override
    public List<V> zrangebylex(K key, Range<? extends V> range) {
        return null;
    }

    @Override
    public List<V> zrangebylex(K key, String min, String max, long offset, long count) {
        return null;
    }

    @Override
    public List<V> zrangebylex(K key, Range<? extends V> range, Limit limit) {
        return null;
    }

    @Override
    public List<V> zrangebyscore(K key, double min, double max) {
        return null;
    }

    @Override
    public List<V> zrangebyscore(K key, String min, String max) {
        return null;
    }

    @Override
    public List<V> zrangebyscore(K key, Range<? extends Number> range) {
        return null;
    }

    @Override
    public List<V> zrangebyscore(K key, double min, double max, long offset, long count) {
        return null;
    }

    @Override
    public List<V> zrangebyscore(K key, String min, String max, long offset, long count) {
        return null;
    }

    @Override
    public List<V> zrangebyscore(K key, Range<? extends Number> range, Limit limit) {
        return null;
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, double min, double max) {
        return null;
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, String min, String max) {
        return null;
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, Range<? extends Number> range) {
        return null;
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, double min, double max, long offset, long count) {
        return null;
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, String min, String max, long offset, long count) {
        return null;
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, Range<? extends Number> range, Limit limit) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, double min, double max) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, String min, String max) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, Range<? extends Number> range) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, double min, double max, long offset, long count) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, String min, String max, long offset, long count) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, Range<? extends Number> range, Limit limit) {
        return null;
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double min, double max) {
        return null;
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String min, String max) {
        return null;
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, Range<? extends Number> range) {
        return null;
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double min, double max, long offset, long count) {
        return null;
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String min, String max, long offset, long count) {
        return null;
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, Range<? extends Number> range, Limit limit) {
        return null;
    }

    @Override
    public Long zrank(K key, V member) {
        return null;
    }

    @Override
    public Long zrem(K key, V... members) {
        return null;
    }

    @Override
    public Long zremrangebylex(K key, String min, String max) {
        return null;
    }

    @Override
    public Long zremrangebylex(K key, Range<? extends V> range) {
        return null;
    }

    @Override
    public Long zremrangebyrank(K key, long start, long stop) {
        return null;
    }

    @Override
    public Long zremrangebyscore(K key, double min, double max) {
        return null;
    }

    @Override
    public Long zremrangebyscore(K key, String min, String max) {
        return null;
    }

    @Override
    public Long zremrangebyscore(K key, Range<? extends Number> range) {
        return null;
    }

    @Override
    public List<V> zrevrange(K key, long start, long stop) {
        return null;
    }

    @Override
    public Long zrevrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrevrangeWithScores(K key, long start, long stop) {
        return null;
    }

    @Override
    public Long zrevrangeWithScores(ScoredValueStreamingChannel<V> channel, K key, long start, long stop) {
        return null;
    }

    @Override
    public List<V> zrevrangebylex(K key, Range<? extends V> range) {
        return null;
    }

    @Override
    public List<V> zrevrangebylex(K key, Range<? extends V> range, Limit limit) {
        return null;
    }

    @Override
    public List<V> zrevrangebyscore(K key, double max, double min) {
        return null;
    }

    @Override
    public List<V> zrevrangebyscore(K key, String max, String min) {
        return null;
    }

    @Override
    public List<V> zrevrangebyscore(K key, Range<? extends Number> range) {
        return null;
    }

    @Override
    public List<V> zrevrangebyscore(K key, double max, double min, long offset, long count) {
        return null;
    }

    @Override
    public List<V> zrevrangebyscore(K key, String max, String min, long offset, long count) {
        return null;
    }

    @Override
    public List<V> zrevrangebyscore(K key, Range<? extends Number> range, Limit limit) {
        return null;
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, double max, double min) {
        return null;
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, String max, String min) {
        return null;
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, Range<? extends Number> range) {
        return null;
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, double max, double min, long offset, long count) {
        return null;
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, String max, String min, long offset, long count) {
        return null;
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, Range<? extends Number> range, Limit limit) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, double max, double min) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, String max, String min) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, Range<? extends Number> range) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, double max, double min, long offset, long count) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, String max, String min, long offset, long count) {
        return null;
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, Range<? extends Number> range, Limit limit) {
        return null;
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double max, double min) {
        return null;
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String max, String min) {
        return null;
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, Range<? extends Number> range) {
        return null;
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double max, double min, long offset, long count) {
        return null;
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String max, String min, long offset, long count) {
        return null;
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, Range<? extends Number> range, Limit limit) {
        return null;
    }

    @Override
    public Long zrevrank(K key, V member) {
        return null;
    }

    @Override
    public ScoredValueScanCursor<V> zscan(K key) {
        return null;
    }

    @Override
    public ScoredValueScanCursor<V> zscan(K key, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public ScoredValueScanCursor<V> zscan(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public ScoredValueScanCursor<V> zscan(K key, ScanCursor scanCursor) {
        return null;
    }

    @Override
    public StreamScanCursor zscan(ScoredValueStreamingChannel<V> channel, K key) {
        return null;
    }

    @Override
    public StreamScanCursor zscan(ScoredValueStreamingChannel<V> channel, K key, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public StreamScanCursor zscan(ScoredValueStreamingChannel<V> channel, K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return null;
    }

    @Override
    public StreamScanCursor zscan(ScoredValueStreamingChannel<V> channel, K key, ScanCursor scanCursor) {
        return null;
    }

    @Override
    public Double zscore(K key, V member) {
        return null;
    }

    @Override
    public Long zunionstore(K destination, K... keys) {
        return null;
    }

    @Override
    public Long zunionstore(K destination, ZStoreArgs storeArgs, K... keys) {
        return null;
    }

    @Override
    public Long xack(K key, K group, String... messageIds) {
        return null;
    }

    @Override
    public String xadd(K key, Map<K, V> body) {
        return null;
    }

    @Override
    public String xadd(K key, XAddArgs args, Map<K, V> body) {
        return null;
    }

    @Override
    public String xadd(K key, Object... keysAndValues) {
        return null;
    }

    @Override
    public String xadd(K key, XAddArgs args, Object... keysAndValues) {
        return null;
    }

    @Override
    public List<StreamMessage<K, V>> xclaim(K key, Consumer<K> consumer, long minIdleTime, String... messageIds) {
        return null;
    }

    @Override
    public List<StreamMessage<K, V>> xclaim(K key, Consumer<K> consumer, XClaimArgs args, String... messageIds) {
        return null;
    }

    @Override
    public Long xdel(K key, String... messageIds) {
        return null;
    }

    @Override
    public String xgroupCreate(XReadArgs.StreamOffset<K> streamOffset, K group) {
        return null;
    }

    @Override
    public String xgroupCreate(XReadArgs.StreamOffset<K> streamOffset, K group, XGroupCreateArgs args) {
        return null;
    }

    @Override
    public Boolean xgroupDelconsumer(K key, Consumer<K> consumer) {
        return null;
    }

    @Override
    public Boolean xgroupDestroy(K key, K group) {
        return null;
    }

    @Override
    public String xgroupSetid(XReadArgs.StreamOffset<K> streamOffset, K group) {
        return null;
    }

    @Override
    public List<Object> xinfoStream(K key) {
        return null;
    }

    @Override
    public List<Object> xinfoGroups(K key) {
        return null;
    }

    @Override
    public List<Object> xinfoConsumers(K key, K group) {
        return null;
    }

    @Override
    public Long xlen(K key) {
        return null;
    }

    @Override
    public List<Object> xpending(K key, K group) {
        return null;
    }

    @Override
    public List<Object> xpending(K key, K group, Range<String> range, Limit limit) {
        return null;
    }

    @Override
    public List<Object> xpending(K key, Consumer<K> consumer, Range<String> range, Limit limit) {
        return null;
    }

    @Override
    public List<StreamMessage<K, V>> xrange(K key, Range<String> range) {
        return null;
    }

    @Override
    public List<StreamMessage<K, V>> xrange(K key, Range<String> range, Limit limit) {
        return null;
    }

    @Override
    public List<StreamMessage<K, V>> xread(XReadArgs.StreamOffset<K>... streams) {
        return null;
    }

    @Override
    public List<StreamMessage<K, V>> xread(XReadArgs args, XReadArgs.StreamOffset<K>... streams) {
        return null;
    }

    @Override
    public List<StreamMessage<K, V>> xreadgroup(Consumer<K> consumer, XReadArgs.StreamOffset<K>... streams) {
        return null;
    }

    @Override
    public List<StreamMessage<K, V>> xreadgroup(Consumer<K> consumer, XReadArgs args, XReadArgs.StreamOffset<K>... streams) {
        return null;
    }

    @Override
    public List<StreamMessage<K, V>> xrevrange(K key, Range<String> range) {
        return null;
    }

    @Override
    public List<StreamMessage<K, V>> xrevrange(K key, Range<String> range, Limit limit) {
        return null;
    }

    @Override
    public Long xtrim(K key, long count) {
        return null;
    }

    @Override
    public Long xtrim(K key, boolean approximateTrimming, long count) {
        return null;
    }

    @Override
    public Long append(K key, V value) {
        return null;
    }

    @Override
    public Long bitcount(K key) {
        return null;
    }

    @Override
    public Long bitcount(K key, long start, long end) {
        return null;
    }

    @Override
    public List<Long> bitfield(K key, BitFieldArgs bitFieldArgs) {
        return null;
    }

    @Override
    public Long bitpos(K key, boolean state) {
        return null;
    }

    @Override
    public Long bitpos(K key, boolean state, long start) {
        return null;
    }

    @Override
    public Long bitpos(K key, boolean state, long start, long end) {
        return null;
    }

    @Override
    public Long bitopAnd(K destination, K... keys) {
        return null;
    }

    @Override
    public Long bitopNot(K destination, K source) {
        return null;
    }

    @Override
    public Long bitopOr(K destination, K... keys) {
        return null;
    }

    @Override
    public Long bitopXor(K destination, K... keys) {
        return null;
    }

    @Override
    public Long decr(K key) {
        return null;
    }

    @Override
    public Long decrby(K key, long amount) {
        return null;
    }

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public Long getbit(K key, long offset) {
        return null;
    }

    @Override
    public V getrange(K key, long start, long end) {
        return null;
    }

    @Override
    public V getset(K key, V value) {
        return null;
    }

    @Override
    public Long incr(K key) {
        return null;
    }

    @Override
    public Long incrby(K key, long amount) {
        return null;
    }

    @Override
    public Double incrbyfloat(K key, double amount) {
        return null;
    }

    @Override
    public List<KeyValue<K, V>> mget(K... keys) {
        return null;
    }

    @Override
    public Long mget(KeyValueStreamingChannel<K, V> channel, K... keys) {
        return null;
    }

    @Override
    public String mset(Map<K, V> map) {
        return null;
    }

    @Override
    public Boolean msetnx(Map<K, V> map) {
        return null;
    }

    @Override
    public String set(K key, V value) {
        return null;
    }

    @Override
    public String set(K key, V value, SetArgs setArgs) {
        return null;
    }

    @Override
    public Long setbit(K key, long offset, int value) {
        return null;
    }

    @Override
    public String setex(K key, long seconds, V value) {
        return null;
    }

    @Override
    public String psetex(K key, long milliseconds, V value) {
        return null;
    }

    @Override
    public Boolean setnx(K key, V value) {
        return null;
    }

    @Override
    public Long setrange(K key, long offset, V value) {
        return null;
    }

    @Override
    public StringMatchResult stralgoLcs(StrAlgoArgs strAlgoArgs) {
        return null;
    }

    @Override
    public Long strlen(K key) {
        return null;
    }

    @Override
    public String discard() {
        return null;
    }

    @Override
    public TransactionResult exec() {
        return null;
    }

    @Override
    public String multi() {
        return null;
    }

    @Override
    public String watch(K... keys) {
        return null;
    }

    @Override
    public String unwatch() {
        return null;
    }
}
