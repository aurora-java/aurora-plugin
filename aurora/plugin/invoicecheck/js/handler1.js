/**
 * @Author: xuzhao
 * @Email: mailto:zhao.xu@hand-china.com
 * @Description: 请求验证码前的处理
 * @Time: 2017/10/17 17:20
 */
var swjginfo = getSwjg(fpdm, 0),
    iterationCount = 100,
    keySize = 128,
    fplx = "99";
ip = swjginfo[1];
var rad = Math.random();
yzmQueryUrl = ip + "/yzmQuery";
queryUrl = ip + "/query";
iv = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);
salt = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);
var aesUtil = new AesUtil(keySize, iterationCount);
fplx = alxd(fpdm);//发票类型已从二维码信息中获得，此处js方法无用
avai(fplx);