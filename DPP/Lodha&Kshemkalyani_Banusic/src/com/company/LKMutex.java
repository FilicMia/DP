package com.company;

import java.util.*;

public class LKMutex extends Process implements Lock {

    private int SeqNum;
    private boolean[] RV;
    private int MAX_NUM;
    private Queue<Pair<Integer, Integer>> LRQ;
    private Queue<Pair<Integer, Integer>> pending;

    public LKMutex(Linker initComm) {
        super(initComm);
        SeqNum = 0;
        RV = new boolean[N];
        for (int j = 0; j < N; j++)
            RV[j] = false;
        LRQ = new PriorityQueue<>(N, (o1, o2) -> {
            if (o1.getFirst().equals(o2.getFirst()))
                return o1.getSecond().compareTo(o2.getSecond());
            if (o1.getFirst() < o2.getFirst())
                return -1;
            return 1;
        });
        pending = new PriorityQueue<>(N, (o1, o2) -> {
            if (o1.getFirst().equals(o2.getFirst()))
                return o1.getSecond().compareTo(o2.getSecond());
            if (o1.getFirst() < o2.getFirst())
                return -1;
            return 1;
        });
        MAX_NUM = 0;
    }


    public synchronized void requestCS() {
        SeqNum = MAX_NUM + 1;
        while (!LRQ.isEmpty() || !pending.isEmpty()) {
            LRQ.poll();
            pending.poll();
        }
        LRQ.add(new Pair<>(SeqNum, myId));
        for (int j = 0; j < N; j++)
            RV[j] = false;
        RV[myId] = true;
        broadcastMsg("request", SeqNum);

        for (int j = 0; j < N; j++)
            RV[j] = false;
        RV[myId] = true;

        while (!checkExecuteCS())
            myWait();
    }

    public synchronized void releaseCS() {
        if (LRQ.peek().getSecond() == myId)
            LRQ.poll();

        if (!LRQ.isEmpty())
            sendMsg(LRQ.poll().getSecond(), "flush", SeqNum);

        while (!pending.isEmpty())
            sendMsg(pending.poll().getSecond(), "okay", SeqNum);

    }

    private boolean checkExecuteCS() {
        for (int j = 0; j < N; j++)
            if (!RV[j]) return false;
        return !(LRQ.isEmpty() || LRQ.peek().getSecond() != myId);
    }

    public synchronized void handleMsg(Msg m, int src, String tag) {
        int currSeqNum = m.getMessageInt();
        Pair<Integer, Integer> t;
        switch (tag) {
            case "request":
                if (MAX_NUM < currSeqNum)
                    MAX_NUM = currSeqNum;

                if (RV[myId]) {
                    if (!RV[src]) {
                        RV[src] = true;
                        LRQ.add(new Pair<>(currSeqNum, src));
                    } else {
                        pending.add(new Pair<>(currSeqNum, src));
                    }
                    if (checkExecuteCS()) {
                        notify();
                    }
                } else {
                    sendMsg(src,"okay", SeqNum);
                }
                break;
            case "okay":
                RV[src] = true;
                t = new Pair<>(-1, -1);
                while (!LRQ.isEmpty() && t.getFirst() <= currSeqNum && t.getSecond() != myId) {
                    t = LRQ.peek();
                    if (t.getFirst() <= currSeqNum && t.getSecond() != myId) LRQ.poll();
                }
                if (checkExecuteCS()) notify();
                break;
            case "flush":
                RV[src] = true;
                t = new Pair<>(-1, -1);
                while (!LRQ.isEmpty() && t.getFirst() <= currSeqNum && t.getSecond() != myId) {
                    t = LRQ.peek();
                    if (t.getFirst() <= currSeqNum && t.getSecond() != myId) LRQ.poll();
                }
                if (checkExecuteCS()) notify();
        }
    }
}