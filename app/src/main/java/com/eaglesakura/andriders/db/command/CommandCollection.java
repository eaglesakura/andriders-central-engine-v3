package com.eaglesakura.andriders.db.command;

import com.eaglesakura.andriders.command.CommandKey;
import com.eaglesakura.lambda.Matcher1;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandCollection {
    final List<CommandData> mCommands;

    public CommandCollection(List<CommandData> commands) {
        mCommands = commands;
    }

    /**
     * 指定したKeyに一致するコマンドを取得するか、nullを返却する
     */
    @Nullable
    public CommandData getOrNull(CommandKey key) {
        List<CommandData> list = list(it -> it.getKey().equals(key));
        if (!list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * 条件にマッチするコマンドを全て取得する
     *
     * @param matcher 条件
     */
    public List<CommandData> list(Matcher1<CommandData> matcher) {
        try {
            List<CommandData> result = new ArrayList<>();
            for (CommandData data : mCommands) {
                if (matcher.match(data)) {
                    result.add(data);
                }
            }

            Collections.sort(result, (l, r) -> {
                if (l.getCategory() != r.getCategory()) {
                    return Integer.compare(l.getCategory(), r.getCategory());
                } else {
                    return l.getKey().getKey().compareTo(r.getKey().getKey());
                }
            });
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
