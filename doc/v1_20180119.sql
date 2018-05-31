CREATE TABLE `t_account` (
  `oid` varchar(64) NOT NULL COMMENT 'id',
  `accountNo` varchar(64) DEFAULT '' COMMENT '账户号',
  `accountType` varchar(4) DEFAULT '' COMMENT '01--基本账户，02--提现冻结账户，03--充值冻结账户，04:投注冻结账户，05：平台账户',
  `balance` decimal(19,2) DEFAULT '0.00' COMMENT '账户余额',
  `rechargeFrozenBalance` decimal(19,2) DEFAULT '0.00' COMMENT '充值冻结',
  `withdrawFrozenBalance` decimal(19,2) DEFAULT '0.00' COMMENT '提现冻结',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `status` varchar(32) DEFAULT '' COMMENT '01:正常,02:冻结',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `userOid` varchar(64) DEFAULT '' COMMENT '用户id',
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `t_game` (
  `oid` varchar(64) NOT NULL COMMENT 'id',
  `closeAmount` decimal(19,2) DEFAULT '0.00' COMMENT '闭盘价',
  `code` varchar(64) DEFAULT '' COMMENT 'code',
  `count` int(11) DEFAULT '0' COMMENT '一天中的场次',
  `endTime` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `fundingPool` decimal(19,2) DEFAULT '0.00' COMMENT '资金总额',
  `name` varchar(255) DEFAULT '' COMMENT '游戏名',
  `openAmount` decimal(19,2) DEFAULT '0.00' COMMENT '开盘价',
  `result` varchar(64) DEFAULT '' COMMENT '比赛结果 win:胜(涨) lose:负(跌) draw:平',
  `startTime` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `status` varchar(4) DEFAULT '1' COMMENT '状态 01:未开始,02:进行中,03:已结束',
  `type` varchar(4) DEFAULT '1' COMMENT '比赛类型 01:球赛 02:猜涨跌',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `t_game_properties` (
  `oid` varchar(64) NOT NULL COMMENT 'id',
  `amount` decimal(19,2) DEFAULT '0.00' COMMENT '投资总额',
  `gameId` varchar(64) DEFAULT '' COMMENT '游戏id',
  `proKey` varchar(64) DEFAULT '' COMMENT '胜:win 负:lose 平:draw',
  `proValue` decimal(19,2) DEFAULT '0.00' COMMENT '赔率',
  `supportCount` int(11) DEFAULT '0' COMMENT '支持人数',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `t_order` (
  `oid` varchar(64) NOT NULL COMMENT 'id',
  `accountOid` varchar(64) DEFAULT '' COMMENT '资金账号id',
  `amount` decimal(19,2) DEFAULT '0.00' COMMENT '订单金额',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gameId` varchar(64) DEFAULT '' COMMENT '游戏id',
  `orderNo` varchar(64) DEFAULT '' COMMENT '订单号',
  `proKey` varchar(64) DEFAULT '' COMMENT '投注项',
  `status` varchar(64) DEFAULT '' COMMENT '状态 01:已投注,02:已完成',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `userOid` varchar(64) DEFAULT '' COMMENT '用户id',
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `t_payment` (
  `oid` varchar(64) NOT NULL COMMENT 'id',
  `accountOid` varchar(64) DEFAULT '' COMMENT '资金账号id',
  `amount` decimal(19,2) DEFAULT '0.00' COMMENT '交易金额',
  `auditStatus` varchar(64) DEFAULT '' COMMENT '审核状态 01:未审核,02:审核通过,03:审核拒绝',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `direction` varchar(4) DEFAULT '' COMMENT '金额方向，01:收入 02:支出',
  `gameId` varchar(64) DEFAULT '' COMMENT '游戏id',
  `orderNo` varchar(64) DEFAULT '' COMMENT '订单号',
  `payNo` varchar(64) DEFAULT '' COMMENT '交易单号',
  `remark` varchar(1024) DEFAULT '' COMMENT '备注',
  `status` varchar(4) DEFAULT '' COMMENT '状态 01:已创建,02:待审核,03:已完成,04:退款中,05:已退款',
  `tradeType` varchar(4) DEFAULT '' COMMENT '交易类型,01:注册赠送,02:充值,03:提现,04:投注,05:盈利',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `userOid` varchar(64) DEFAULT '' COMMENT '用户id',
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `t_sys_user` (
  `oid` varchar(64) NOT NULL COMMENT 'id',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `idNum` varchar(64) DEFAULT '' COMMENT '身份证',
  `phone` varchar(16) DEFAULT '' COMMENT '手机号',
  `realName` varchar(64) DEFAULT '' COMMENT '真实姓名',
  `role` varchar(64) DEFAULT '' COMMENT '角色 admin 超级管理员,operation 运营人员',
  `salt` varchar(64) DEFAULT '' COMMENT '随机密钥盐值',
  `status` varchar(64) DEFAULT '' COMMENT '状态 normal 正常, forbidden 冻结',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `userName` varchar(64) DEFAULT '' COMMENT '用户名',
  `userPwd` varchar(64) DEFAULT '' COMMENT '用户密码',
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `t_user` (
  `oid` varchar(64) NOT NULL COMMENT 'id',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `idNum` varchar(64) DEFAULT '' COMMENT '身份证',
  `payPwd` varchar(64) DEFAULT '' COMMENT '字符密码',
  `paySalt` varchar(64) DEFAULT '' COMMENT '支付密码盐值',
  `phone` varchar(16) DEFAULT '' COMMENT '手机号',
  `realName` varchar(64) DEFAULT '' COMMENT '真实姓名',
  `salt` varchar(64) DEFAULT '' COMMENT '登录密码盐值',
  `status` varchar(64) DEFAULT '' COMMENT '状态 normal 正常, forbidden 冻结',
  `uid` varchar(64) DEFAULT '' COMMENT '邀请码',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `userAccount` varchar(64) DEFAULT '' COMMENT '用户账号',
  `userPwd` varchar(64) DEFAULT '' COMMENT '支付密码',
  `userType` varchar(64) DEFAULT '1' COMMENT '用户类型 01:普通用户,02:系统账号',
  PRIMARY KEY (`oid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
