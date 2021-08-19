package org.example.job.core.biz.impl;

import org.example.job.core.biz.ExecutorBiz;
import org.example.job.core.biz.model.*;
//TODO
public class ExecutorBizImpl implements ExecutorBiz {
    @Override
    public ReturnT<String> beat() {
        return null;
    }

    @Override
    public ReturnT<String> idleBeat(IdleBeatParam idleBeatParam) {
        return null;
    }

    @Override
    public ReturnT<String> run(TriggerParam triggerParam) {
        return null;
    }

    @Override
    public ReturnT<String> kill(KillParam killParam) {
        return null;
    }

    @Override
    public ReturnT<LogResult> log(LogParam logParam) {
        return null;
    }
}
