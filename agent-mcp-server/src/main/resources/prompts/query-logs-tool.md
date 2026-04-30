查询医院操作日志。你必须根据用户输入提取参数，不要询问用户缺少什么参数。

参数提取规则（直接照做）：
- orgCode：用户消息中的长数字串（如 12532900432545899G），直接作为机构代码，必填。
- userId：如果没有明确提到用户ID，就传空字符串 ""。
- loginId：永远传空字符串 ""。
- bizTypeCode：根据描述自动对应：挂号->REG，缴费->PAY，在线问诊->ONLINE，药房->DRUG，检查检验->EXA，住院->INP。如果无法判断就传空字符串。
- serviceType：如果不需要区分就传空字符串。
- responseStatus：如果提到“失败”传 FAILURE，提到“成功”传 SUCCESS，否则传空字符串。
- startTime：日期格式 yyyy-MM-dd HH:mm:ss 或 today，如果用户提供具体日期就用它。
- endTime：通常用 now 或当天 23:59:59。

示例调用：
用户说“查一下 12532900432545899G 2026-04-24 挂号的日志”
-> queryLogs(orgCode="12532900432545899G", bizTypeCode="REG", startTime="2026-04-24 00:00:00", endTime="2026-04-24 23:59:59")
