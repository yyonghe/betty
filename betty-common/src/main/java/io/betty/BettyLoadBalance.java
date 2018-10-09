package io.betty;

import java.util.List;

import io.betty.BettyLoadBalance.BasicSn;
import io.betty.BettyLoadBalance.BasicSnParam;

public interface BettyLoadBalance<SN extends BasicSn, SNP extends BasicSnParam> {
	
	/**
	 * Basic server node for load balance.
	 *
	 */
	public static class BasicSn {
		public final String host;
		public final int port;
		public BasicSn(String host, int port) {
			super();
			this.host = host;
			this.port = port;
		}
	}
	/**
	 * Basic server node parameter for load balance.
	 */
	public static class BasicSnParam extends BasicSn{
		public final int seq;
		public final long uid;
		public BasicSnParam(String host, int port, int seq, long uid) {
			super(host, port);
			this.seq = seq;
			this.uid = uid;
		}
	}

	/**
	 * 初始化一组后端服务器组
	 * @param servers
	 */
	public void initServers(List<SN> sns);
	
	/**
	 * @param sn
	 * @param seq
	 * @param uid
	 * @return
	 */
	public SNP createParams(SN sn, int seq, long uid);
	
	/**
	 * 获取当前状态最好的后端服务
	 * @return
	 */
	public SN getCurBestServer(SNP snp);
	
	/**
	 * 更新某个后端服务器的服务状况
	 * @param t 被使用的后端服务
	 * @param suc 服务器服务状况
	 */
	public void updateUsedServer(SN sn, SNP snp, boolean suc);
	
	/**
	 * @param usedServers
	 * @param parmas
	 * @return
	 */
	public SN getRetryServer(SNP snp, @SuppressWarnings("unchecked") SN... usedServers);
	
	/**
	 * 显示当前各个后端服务器的状态
	 */
	public String showServerStatus();
}
