/**
 * @Author: xuzhao
 * @Email: mailto:zhao.xu@hand-china.com
 * @Description: 处理发票请求响应报文的js
 * @Time: 2017/10/18 9:57
 */
var t = jsonData.key5;//var t = "var aa17=fpdm+\"≡\"+fphm+\"≡\"+swjgmc+\"≡\"+jsonData.key2+\"≡\"+yzmSj"
eval(t);
var hwxx = jsonData.key3;
var jmbz = "";
if (jsonData.key4.trim() != '') {
    jmbz = aesUtil.decrypt(jsonData.key8, jsonData.key7, jsonData.key9, jsonData.key4);//解密备注
}
if(!jmbz || jmbz.length == 0){
    jmbz = " ";
}
var jmsort = aesUtil.decrypt(jsonData.key8, jsonData.key7, jsonData.key9, jsonData.key10);//解密排序顺序
var tt = jsonData.key6;//var tt = "var result={\"template\":0,\"fplx\":fplx,\"fpxx\":fpxx,\"hwxx\":hwxx,\"jmbz\":jmbz,\"sort\":jmsort}";
eval(tt);
jsonResult = JSON.stringify(result);//调试使用
var rules = rule.split('☺');
decryptFpyzResponseResult(result);
var checkRsult = getInvoiceCheckResult(fpxx, hwxxs);