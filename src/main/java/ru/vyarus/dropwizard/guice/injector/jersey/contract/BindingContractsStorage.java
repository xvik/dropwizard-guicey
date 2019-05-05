package ru.vyarus.dropwizard.guice.injector.jersey.contract;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Key;

import java.lang.reflect.Type;
import java.util.*;

/**
 * DI frameworks like HK2 and CDI allows binding multiple keys to one service, but in guice it could be only one key.
 * Contracts are used a lot by jersey, so have to emulate them.
 *
 * @author Vyacheslav Rusakov
 * @since 23.04.2019
 */
public class BindingContractsStorage {

    // todo replace with multibindings!

    private Multimap<Key, Type> contractsInfo = Multimaps.synchronizedMultimap(LinkedHashMultimap.create());
    private Multimap<Type, Key> index = Multimaps.synchronizedMultimap(LinkedHashMultimap.create());

    public void register(Key key, Collection<Type> contracts) {
        contractsInfo.putAll(key, contracts);
        for (Type contract : contracts) {
            // prevent duplicates
            if (!index.containsEntry(contract, key)) {
                index.put(contract, key);
            }
        }
    }

    public boolean containsKey(Key key) {
        return contractsInfo.containsKey(key);
    }

    public List<Key> findByContract(Type contract) {
        return new ArrayList<>(index.get(contract));
    }

    public Set<Type> getContracts(Key key) {
        return new HashSet<>(contractsInfo.get(key));
    }
}
