/*
 * Copyright (C) 2020 Jakub Ksiezniak
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.jksiezni.xpra.config;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.plugins.RxJavaPlugins;

@Dao
public interface ConnectionDao {

    @Query("SELECT * FROM ServerDetails")
    LiveData<List<ServerDetails>> getAll();

    @Query("SELECT * FROM ServerDetails WHERE id = :id")
    Single<ServerDetails> getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(ServerDetails config);

    @Delete
    void delete(ServerDetails config);

    default SingleObserver<ServerDetails> save() {
        return new DisposableSingleObserver<ServerDetails>() {
            @Override
            public void onSuccess(ServerDetails deviceConfig) {
                save(deviceConfig);
            }

            @Override
            public void onError(Throwable e) {
                RxJavaPlugins.onError(e);
            }
        };
    }
}
