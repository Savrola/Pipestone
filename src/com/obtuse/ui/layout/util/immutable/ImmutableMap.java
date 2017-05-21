/*
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.obtuse.ui.layout.util.immutable;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 %%% Something clever goes here.
 */
public class ImmutableMap<K,V> implements Map<K,V>, Serializable {
    private static final long serialVersionUID = -1034234728574286014L;

    private final Map<? extends K, ? extends V> m;

    ImmutableMap(Map<? extends K, ? extends V> m) {
	if (m==null)
	    throw new NullPointerException();
	this.m = m;
    }

    public int size()                        {return m.size();}
    public boolean isEmpty()                 {return m.isEmpty();}
    public boolean containsKey(Object key)   {return m.containsKey(key);}
    public boolean containsValue(Object val) {return m.containsValue(val);}
    public V get(Object key)                 {return m.get(key);}

    public V put(K key, V value) {
	throw new UnsupportedOperationException();
    }
    public V remove(Object key) {
	throw new UnsupportedOperationException();
    }
    public void putAll(Map<? extends K, ? extends V> m) {
	throw new UnsupportedOperationException();
    }
    public void clear() {
	throw new UnsupportedOperationException();
    }

    private transient Set<K> keySet;
    private transient Set<Map.Entry<K,V>> entrySet;
    private transient Collection<V> values;

    public Set<K> keySet() {
	if (keySet==null)
	    keySet = Collections.unmodifiableSet(m.keySet());
	return keySet;
    }

    public Set<Map.Entry<K,V>> entrySet() {
	if (entrySet==null)
	    entrySet = new ImmutableMap.UnmodifiableEntrySet<>( m.entrySet());
	return entrySet;
    }

    public Collection<V> values() {
	if (values==null)
	    values = Collections.unmodifiableCollection(m.values());
	return values;
    }

    /**
     * Returns true if the specified arguments are equal, or both null.
     *
     * NB: Do not replace with Object.equals until JDK-8015417 is resolved.
     */
    static boolean eq(Object o1, Object o2) {
	return o1==null ? o2==null : o1.equals(o2);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {return o == this || m.equals( o);}
    public int hashCode()           {return m.hashCode();}
    public String toString()        {return m.toString();}

    // Override default methods in Map
    @Override
    @SuppressWarnings("unchecked")
    public V getOrDefault(Object k, V defaultValue) {
	// Safe cast as we don't change the value
	return ((Map<K, V>)m).getOrDefault(k, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
	m.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
	throw new UnsupportedOperationException();
    }

    @Override
    public V putIfAbsent(K key, V value) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object key, Object value) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
	throw new UnsupportedOperationException();
    }

    @Override
    public V replace(K key, V value) {
	throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
	throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(K key,
	    BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
	throw new UnsupportedOperationException();
    }

    @Override
    public V compute(K key,
	    BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
	throw new UnsupportedOperationException();
    }

    @Override
    public V merge(K key, V value,
	    BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
	throw new UnsupportedOperationException();
    }

    /**
     * We need this class in addition to ImmutableSet as
     * Map.Entries themselves permit modification of the backing Map
     * via their setValue operation.  This class is subtle: there are
     * many possible attacks that must be thwarted.
     *
     * @serial include
     */
    static class UnmodifiableEntrySet<K,V>
	    extends ImmutableSet<Entry<K,V>> {
	private static final long serialVersionUID = 7854390611657943733L;

	@SuppressWarnings({"unchecked", "rawtypes"})
	UnmodifiableEntrySet(Set<? extends Map.Entry<? extends K, ? extends V>> s) {
	    // Need to cast to raw in order to work around a limitation in the type system
	    super((Set)s);
	}

	static <K, V> Consumer<Entry<K, V>> entryConsumer(Consumer<? super Entry<K, V>> action) {
	    return e -> action.accept(new ImmutableMap.UnmodifiableEntrySet.UnmodifiableEntry<>( e));
	}

	public void forEach(Consumer<? super Entry<K, V>> action) {
	    Objects.requireNonNull( action);
	    c.forEach(entryConsumer(action));
	}

	static final class UnmodifiableEntrySetSpliterator<K, V>
		implements Spliterator<Entry<K,V>> {
	    final Spliterator<Map.Entry<K, V>> s;

	    UnmodifiableEntrySetSpliterator(Spliterator<Entry<K, V>> s) {
		this.s = s;
	    }

	    @Override
	    public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
		Objects.requireNonNull(action);
		return s.tryAdvance(entryConsumer(action));
	    }

	    @Override
	    public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
		Objects.requireNonNull(action);
		s.forEachRemaining(entryConsumer(action));
	    }

	    @Override
	    public Spliterator<Entry<K, V>> trySplit() {
		Spliterator<Entry<K, V>> split = s.trySplit();
		return split == null
			? null
			: new ImmutableMap.UnmodifiableEntrySet.UnmodifiableEntrySetSpliterator<>( split);
	    }

	    @Override
	    public long estimateSize() {
		return s.estimateSize();
	    }

	    @Override
	    public long getExactSizeIfKnown() {
		return s.getExactSizeIfKnown();
	    }

	    @Override
	    public int characteristics() {
		return s.characteristics();
	    }

	    @Override
	    public boolean hasCharacteristics(int characteristics) {
		return s.hasCharacteristics(characteristics);
	    }

	    @Override
	    public Comparator<? super Entry<K, V>> getComparator() {
		return s.getComparator();
	    }
	}

	@SuppressWarnings("unchecked")
	public Spliterator<Entry<K,V>> spliterator() {
	    return new ImmutableMap.UnmodifiableEntrySet.UnmodifiableEntrySetSpliterator<>(
		    (Spliterator<Map.Entry<K, V>>) c.spliterator());
	}

	@Override
	public Stream<Entry<K,V>> stream() {
	    return StreamSupport.stream( spliterator(), false);
	}

	@Override
	public Stream<Entry<K,V>> parallelStream() {
	    return StreamSupport.stream(spliterator(), true);
	}

	public Iterator<Map.Entry<K,V>> iterator() {
	    return new Iterator<Map.Entry<K,V>>() {
		private final Iterator<? extends Map.Entry<? extends K, ? extends V>> i = c.iterator();

		public boolean hasNext() {
		    return i.hasNext();
		}
		public Map.Entry<K,V> next() {
		    return new ImmutableMap.UnmodifiableEntrySet.UnmodifiableEntry<>( i.next());
		}
		public void remove() {
		    throw new UnsupportedOperationException();
		}
	    };
	}

	@SuppressWarnings("unchecked")
	public Object[] toArray() {
	    Object[] a = c.toArray();
	    for (int i=0; i<a.length; i++)
		a[i] = new ImmutableMap.UnmodifiableEntrySet.UnmodifiableEntry<>( (Map.Entry<? extends K, ? extends V>)a[i]);
	    return a;
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
	    // We don't pass a to c.toArray, to avoid window of
	    // vulnerability wherein an unscrupulous multithreaded client
	    // could get his hands on raw (unwrapped) Entries from c.
	    Object[] arr = c.toArray(a.length==0 ? a : Arrays.copyOf(a, 0));

	    for (int i=0; i<arr.length; i++)
		arr[i] = new ImmutableMap.UnmodifiableEntrySet.UnmodifiableEntry<>( (Map.Entry<? extends K, ? extends V>)arr[i]);

	    if (arr.length > a.length)
		return (T[])arr;

	    System.arraycopy(arr, 0, a, 0, arr.length);
	    if (a.length > arr.length)
		a[arr.length] = null;
	    return a;
	}

	/**
	 * This method is overridden to protect the backing set against
	 * an object with a nefarious equals function that senses
	 * that the equality-candidate is Map.Entry and calls its
	 * setValue method.
	 */
	public boolean contains(Object o) {
	    if (!(o instanceof Map.Entry))
		return false;
	    return c.contains(
		    new ImmutableMap.UnmodifiableEntrySet.UnmodifiableEntry<>( (Map.Entry<?,?>) o));
	}

	/**
	 * The next two methods are overridden to protect against
	 * an unscrupulous List whose contains(Object o) method senses
	 * when o is a Map.Entry, and calls o.setValue.
	 */
	public boolean containsAll(Collection<?> coll) {
	    for (Object e : coll) {
		if (!contains(e)) // Invokes safe contains() above
		    return false;
	    }
	    return true;
	}
	public boolean equals(Object o) {
	    if (o == this)
		return true;

	    if (!(o instanceof Set))
		return false;
	    Set<?> s = (Set<?>) o;
	    if (s.size() != c.size())
		return false;
	    return containsAll(s); // Invokes safe containsAll() above
	}

	/**
	 * This "wrapper class" serves two purposes: it prevents
	 * the client from modifying the backing Map, by short-circuiting
	 * the setValue method, and it protects the backing Map against
	 * an ill-behaved Map.Entry that attempts to modify another
	 * Map Entry when asked to perform an equality check.
	 */
	private static class UnmodifiableEntry<K,V> implements Map.Entry<K,V> {
	    private Map.Entry<? extends K, ? extends V> e;

	    UnmodifiableEntry(Map.Entry<? extends K, ? extends V> e)
	    {this.e = Objects.requireNonNull(e);}

	    public K getKey()        {return e.getKey();}
	    public V getValue()      {return e.getValue();}
	    public V setValue(V value) {
		throw new UnsupportedOperationException();
	    }
	    public int hashCode()    {return e.hashCode();}
	    public boolean equals(Object o) {
		if (this == o)
		    return true;
		if (!(o instanceof Map.Entry))
		    return false;
		Map.Entry<?,?> t = (Map.Entry<?,?>)o;
		return eq(e.getKey(),   t.getKey()) &&
		       eq(e.getValue(), t.getValue());
	    }
	    public String toString() {return e.toString();}
	}
    }
}