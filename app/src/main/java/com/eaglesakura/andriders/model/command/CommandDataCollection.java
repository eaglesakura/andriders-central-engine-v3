package com.eaglesakura.andriders.model.command;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.collection.DataCollection;

import android.support.annotation.Nullable;

import java.util.List;

public class CommandDataCollection extends DataCollection<CommandData> {
    public CommandDataCollection(List<CommandData> commands) {
        super(commands);
        setComparator(CommandData.COMPARATOR_ASC);
    }


    /**
     * 指定したKeyに一致するコマンドを取得するか、nullを返却する
     */
    @Nullable
    public CommandData find(CommandKey key) {
        return find(it -> it.getKey().equals(key));
    }

}
