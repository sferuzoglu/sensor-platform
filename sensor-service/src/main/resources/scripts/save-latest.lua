-- KEYS[1] = sensor:<id>:latest
-- ARGV    = sequence, timestamp, value, ttlSeconds
local current = redis.call('HGET', KEYS[1], 'sequence')
if current and tonumber(ARGV[1]) <= tonumber(current) then
    return 0   -- stale or duplicate packet: do not write
end
redis.call('HSET', KEYS[1], 'sequence', ARGV[1], 'timestamp', ARGV[2], 'value', ARGV[3])
redis.call('EXPIRE', KEYS[1], ARGV[4])
return 1       -- accepted