package io.betty.lb;

import java.util.List;

import io.betty.BettyLoadBalance;
import io.betty.BettyLoadBalance.BasicSn;
import io.betty.BettyLoadBalance.BasicSnParam;
import io.betty.util.MiscUtils;

/**
 * 基本的轮询负载均衡，没有容错容灾机制，并且多线程不安全
 *
 */
public class RoundRobineLoadBalance implements BettyLoadBalance<BasicSn, BasicSnParam> {
	
	private BasicSn[] nodes;
	
	private int nServers;
	
	private int nMask;

	@Override
	public void initServers(List<BasicSn> sns) {
		int size0 = sns.size();
		int size = MiscUtils.ceilingNextPowerOfTwo(size0);
		nodes = new BasicSn[size];
		for(int ii = 0; ii<size; ii++) {
			int index = ii % size0;
			BasicSn node = sns.get(index);
			nodes[ii] = node;
		}
		//
		nServers = size;
		nMask = size - 1;
	}

	@Override
	public BasicSnParam createParams(BasicSn sn, int seq, long uid) {
		BasicSnParam p = null;
		if(sn == null) {
			p = new BasicSnParam(null, 0, seq, uid);
		} else {
			p = new BasicSnParam(sn.host, sn.port, seq, uid);
		}
		
		return p;
	}

	@Override
	public BasicSn getCurBestServer(BasicSnParam snp) {
		int index = snp.seq & nMask;
		return nodes[index];
	}

	@Override
	public void updateUsedServer(BasicSn sn, BasicSnParam snp, boolean suc) {
	}

	@Override
	public BasicSn getRetryServer(BasicSnParam snp, BasicSn... usedServers) {
		return getCurBestServer(snp);
	}

	@Override
	public String showServerStatus() {
		return "Server numbers: " + nServers;
	}
}
