
package com.easy.tx.pojo;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface TxCallback<T> {

	@Nullable
	T doInTransaction();

}
