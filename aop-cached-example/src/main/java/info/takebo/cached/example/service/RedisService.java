package info.takebo.cached.example.service;

import info.takebo.cached.service.CacheService;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisException;

import com.google.common.collect.Iterables;

/**
 * @author takecy
 */
public class RedisService implements CacheService {

	@Inject
	private JedisPool pool;

	@Override
	public Set<String> listKeys(String pattern) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.keys(pattern);
		} catch (JedisException e) {
			throw e;
		} finally {
			pool.returnResource(jedis);
		}
	}

	@Override
	public Optional<String> get(String key) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			return Optional.ofNullable(jedis.get(key));
		} catch (JedisException e) {
			throw e;
		} finally {
			pool.returnResource(jedis);
		}
	}

	@Override
	public void delete(Set<String> keys) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			Pipeline p = jedis.pipelined();
			p.del(Iterables.toArray(keys, String.class));
			p.sync();
		} catch (JedisException e) {
			throw e;
		} finally {
			pool.returnResource(jedis);
		}
	}

	@Override
	public void setWithExpire(String key, String value, int expire, TimeUnit unit) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			Pipeline p = jedis.pipelined();
			p.setex(key, (int) unit.toSeconds(expire), value);
			p.sync();
		} catch (JedisException e) {
			throw e;
		} finally {
			pool.returnResource(jedis);
		}
	}

}
