package ru.vyarus.guicey.jdbi3.tx.aop.config;

import ru.vyarus.guicey.jdbi3.tx.InTransaction;
import ru.vyarus.guicey.jdbi3.tx.TxConfig;

import jakarta.inject.Singleton;

/**
 * Transactional config support for default {@link InTransaction} annotation.
 *
 * @author Vyacheslav Rusakov
 * @since 28.09.2018
 */
@Singleton
public class InTransactionTxConfigFactory implements TxConfigFactory<InTransaction> {

    @Override
    public TxConfig build(final InTransaction annotation) {
        return new TxConfig()
                .level(annotation.value())
                .readOnly(annotation.readOnly());
    }
}
