package com.mihealth.db.repositories;

public interface UserRepositoryCustom {
	int setFixedEnabledFor(String uid, boolean enable);
}
