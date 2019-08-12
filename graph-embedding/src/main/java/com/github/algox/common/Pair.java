package com.github.algox.common;

import com.github.algox.utils.CommonUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.*;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

public class Pair<K, V> implements Map.Entry<K, V>, Serializable {
    public final K first;
    public final V second;

    protected Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public static  <X, Y> Pair<X, Y> cons(X x, Y y) {
        return new Pair<>(x, y);
    }

    public static <X> Pair<X, X> cons(X x, X y, boolean swapped) {
        return swapped ? new Pair<>(y, x) : new Pair<>(x, y);
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode() * 13) +
                (second == null ? 0 : second.hashCode() * 17);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        Pair<?, ?> op = (Pair<?, ?>) obj;
        return Objects.equals(op.first, first) &&
                Objects.equals(op.second, second);
    }

    public K getOne() {
        return first;
    }

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "Pair<"+first+","+second+">";
    }

    public List<Object> asList() {
        return Lists.newArrayList(first, second);
    }

    public static <X> List<Pair<X, X>> fromPairs(X... args) {
        if (CommonUtils.isOdd(args.length)) {
            throw new IllegalArgumentException("Array length must be even: " + args.length);
        }
        List<Pair<X, X>> l = new ArrayList<>(args.length / 2);
        for (int i = 0; i < args.length; i += 2) {
            l.add(Pair.cons(args[i], args[i + 1]));
        }
        return l;
    }

    public static <X, Y> List<Pair<X, Y>> fromMap(Map<X, Y> map) {
        List<Pair<X, Y>> l = new ArrayList<>();
        for (Map.Entry<X, Y> m : map.entrySet()) {
            l.add(Pair.cons(m.getKey(), m.getValue()));
        }
        return l;
    }

    private static <X, Y, C extends Collection<Pair<X, Y>>> C fromMapFlatten(C c, Map<? extends X, ? extends Collection<? extends Y>> map) {
        for (Map.Entry<? extends X, ? extends Collection<? extends Y>> me : map.entrySet()) {
            for (Y y : me.getValue()) {
                c.add(Pair.<X, Y>cons(me.getKey(), y));
            }
        }
        return c;
    }

    @Override
    public K getKey() {
        return first;
    }

    @Override
    public V getValue() {
        return second;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return String.valueOf(second);
    }

    public Pair<V, K> swapped() {
        return Pair.cons(second, first);
    }

    public static <K, V> Function<Pair<K, V>, Pair<V, K>> swappedFunction() {
        return new Function<Pair<K, V>, Pair<V, K>>() {
            @Override
            public Pair<V, K> apply(Pair<K, V> kvPair) {
                return kvPair.swapped();
            }
        };
    }

    public static <X extends Comparable<? super X>> Ordering<Pair<X, ?>> firstComparator() {
        return new Ordering<Pair<X, ?>>() {
            @Override public int compare(Pair<X, ?> o1, Pair<X, ?> o2) {
                return CommonUtils.compare(o1.first, o2.first);
            }
        };
    }

    public static <Y extends Comparable<? super Y>> Ordering<Pair<?, Y>> secondComparator() {
        return new Ordering<Pair<?, Y>>() {
            @Override public int compare(Pair<?, Y> o1, Pair<?, Y> o2) {
                return  CommonUtils.compare(o1.second, o2.second);
            }
        };
    }

    public static <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Ordering<Pair<X,Y>> firstThenSecondComparator() {
        return new Ordering<Pair<X,Y>>() {
            @Override public int compare(Pair<X, Y> o1, Pair<X, Y> o2) {
                int k =  CommonUtils.compare(o1.first, o2.first);
                if (k == 0) {
                    k = CommonUtils.compare(o1.second, o2.second);
                }
                return k;
            }
        };
    }

    public static <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Ordering<Pair<X,Y>> secondThenFirstComparator() {
        return new Ordering<Pair<X,Y>>() {
            @Override public int compare(Pair<X, Y> o1, Pair<X, Y> o2) {
                int k = CommonUtils.compare(o1.second, o2.second);
                if (k == 0) {
                    k = CommonUtils.compare(o1.first, o2.first);
                }
                return k;
            }
        };
    }

    public static <X, Y> Comparator<Pair<X,Y>> firstComparator(final Comparator<? super X> comp) {
        return new Comparator<Pair<X,Y>>() {
            @Override public int compare(Pair<X, Y> o1, Pair<X, Y> o2) {
                return comp.compare(o1.first, o2.first);
            }
        };
    }

    public static <X, Y> Comparator<Pair<X,Y>> secondComparator(final Comparator<? super Y> comp) {
        return new Comparator<Pair<X,Y>>() {
            @Override public int compare(Pair<X, Y> o1, Pair<X, Y> o2) {
                return comp.compare(o1.second, o2.second);
            }
        };
    }

    public static <X extends Comparable<? super X>, Y extends Comparable<? super Y>> Comparator<Pair<X,Y>> bothFirstReversedComparator() {
        return new Comparator<Pair<X,Y>>() {
            @Override public int compare(Pair<X, Y> o1, Pair<X, Y> o2) {
                int k = CommonUtils.compare(o2.first, o1.first);
                if (k == 0) {
                    k = CommonUtils.compare(o1.second, o2.second);
                }
                return k;
            }
        };
    }

    private static <X, Y> Map<X, Y> fillMap(Map<X, Y> m, Iterable<? extends Pair<? extends X, ? extends Y>> pairs) {
        for (Pair<? extends X, ? extends Y> p : pairs) {
            m.put(p.first, p.second);
        }
        return m;
    }

    public static <X, Y> List<Pair<X, Y>> cartesianProduct(Collection<X> c1, Collection<Y> c2) {
        return FluentIterable.from(Sets.cartesianProduct(ImmutableSet.copyOf(c1), ImmutableSet.copyOf(c2)))
                .transform(new com.google.common.base.Function<List<Object>, Pair<X, Y>>() {
                    @Override public Pair<X, Y> apply(List<Object> objs) {
                        X x = (X) objs.get(0);
                        Y y = (Y) objs.get(1);
                        return Pair.<X, Y>cons(x, y);
                    }
                })
                .toList();
    }

    public static <X, Y> List<Pair<X, Y>> zip(Collection<X> c1, Collection<Y> c2) {
        return zip(c1, c2, new ArrayList<Pair<X, Y>>(c1.size()), false);
    }

    public static <X, Y> List<Pair<X, Y>> zip(X[] a1, Y[] a2) {
        return zip(ImmutableList.copyOf(a1), ImmutableList.copyOf(a2));
    }

    public static <X, Y> List<Pair<X, Y>> zipUnique(Collection<X> c1, Collection<Y> c2) {
        return zip(c1, c2, new ArrayList<Pair<X, Y>>(), true);
    }

    private static <X, Y> List<Pair<X, Y>> zip(Collection<X> c1, Collection<Y> c2, List<Pair<X, Y>> output, boolean uniqueKeys) {
        int size = c1.size();
        if (size != c2.size()) {
            throw new IllegalArgumentException("Collections must be of same size: " + size + ", " + c2.size());
        }
        Set<X> set = uniqueKeys ? new HashSet<X>() : null;
        Iterator<X> it1 = c1.iterator();
        Iterator<Y> it2 = c2.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            X x = it1.next();
            Y y = it2.next();
            if (set == null || set.add(x)) {
                output.add(Pair.cons(x, y));
            }
        }
        return output;
    }

    public static <X, Y> Iterable<Pair<X, Y>> zipInner(final Iterable<X> first, final Iterable<Y> second) {
        return new Iterable<Pair<X, Y>>() {
            @Override public Iterator<Pair<X, Y>> iterator() {
                final Iterator<X> x = first.iterator();
                final Iterator<Y> y = second.iterator();
                return new Iterator<Pair<X, Y>>() {
                    @Override public boolean hasNext() {
                        return x.hasNext() && y.hasNext();
                    }
                    @Override
                    public Pair<X, Y> next() {
                        return Pair.cons(x.next(), y.next());
                    }
                    @Override
                    public void remove() {
                        x.remove();
                        y.remove();
                    }
                };
            }
        };
    }

    public static <V> com.google.common.base.Function<Pair<?, V>, V> retrieveSecondFunction() {
        return new com.google.common.base.Function<Pair<?, V>, V>() {
            @Override
            public V apply(Pair<?, V> p) {
                return p.second;
            }
        };
    }

    public static <K, V> Iterable<V> unzipSecond(Iterable<Pair<K, V>> pairs) {
        return Iterables.transform(pairs, Pair.<V>retrieveSecondFunction());
    }

    public static <K, V, V2> com.google.common.base.Function<Pair<K, V>, Pair<K, V2>> mapValues(final com.google.common.base.Function<? super V, V2> func) {
        return new com.google.common.base.Function<Pair<K, V>, Pair<K, V2>>() {
            @Override public Pair<K, V2> apply(Pair<K, V> p) {
                return Pair.cons(p.first, func.apply(p.second));
            }
        };
    }

    public static <K> K firstOrNull(Pair<K, ?> pair) {
        return pair != null ? pair.first : null;
    }

    public static <V> V secondOrNull(Pair<?, V> pair) {
        return pair != null ? pair.second : null;
    }

    public static <K, V> Predicate<Pair<K, V>> getFirstPredicate(final Predicate<? super K> pred) {
        return new Predicate<Pair<K, V>>() {
            @Override public boolean apply(Pair<K, V> pair) {
                return pred.apply(pair.first);
            }
        };
    }

    public static <K, V> Predicate<Pair<K, V>> getSecondPredicate(final Predicate<? super V> pred) {
        return new Predicate<Pair<K, V>>() {
            @Override public boolean apply(Pair<K, V> pair) {
                return pred.apply(pair.second);
            }
        };
    }

    public static <K, V> Predicate<Pair<K, V>> getAndPredicate(final Predicate<? super K> firstPred, final Predicate<? super V> secondPred) {
        return new Predicate<Pair<K, V>>() {
            @Override public boolean apply(Pair<K, V> pair) {
                return firstPred.apply(pair.first) && secondPred.apply(pair.second);
            }
        };
    }

    public static <K, V> Predicate<Pair<K, V>> getOrPredicate(final Predicate<? super K> firstPred, final Predicate<? super V> secondPred) {
        return new Predicate<Pair<K, V>>() {
            @Override public boolean apply(Pair<K, V> pair) {
                return firstPred.apply(pair.first) || secondPred.apply(pair.second);
            }
        };
    }

    public static <X, Y> ImmutableList<Pair<X, Y>> toPairList(Iterable<X> values, com.google.common.base.Function<X, Y> func) {
        ImmutableList.Builder<Pair<X, Y>> result = ImmutableList.builder();
        for (X x : values)
            result.add(Pair.cons(x, func.apply(x)));
        return result.build();
    }
}
