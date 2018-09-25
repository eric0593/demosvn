package com.idea.jgw.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.idea.jgw.R;
import com.idea.jgw.test.TransactionEncoder;
import com.xdja.SafeKey.JNIAPI;
import com.xdja.SafeKey.XDJA_SM2_PRIKEY;
import com.xdja.SafeKey.XDJA_SM2_PUBKEY;

import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

public class TEmpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        findViewById(R.id.wallet_sign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walletSign();
            }
        });
        findViewById(R.id.write_key).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.private_key_sign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    void walletSign() {


        //模拟创建一笔交易
        BigDecimal decimal = new BigDecimal(10).multiply(new BigDecimal(10).pow(18));
        BigInteger value = decimal.toBigInteger();
        RawTransaction etherTransaction = RawTransaction.createEtherTransaction(new BigInteger("1"),new BigInteger("1"),new BigInteger("100000"),"0xa2f7174fb5c7ce4a4fe6594c4d167134b64dbac8",value);


        //导入私钥
        ECKeyPair ke = ECKeyPair
                .create(Numeric.hexStringToByteArray("C68FB09938412661E20EBD7E31CAF8C4B658AE939AEA102D41E11545968DC464"));


        //----签名开始
        //签名前处理
        byte[] encode = TransactionEncoder.encode(etherTransaction);
        byte[] messageHash = Hash.sha3(encode);
        System.out.println("messageHash length = " + messageHash.length);

        //由芯片通过私钥来做具体签名
        ECDSASignature sig = ke.sign(messageHash);

        //签名后处理,需要用到公钥匙
        BigInteger publicKey = ke.getPublicKey();
        System.out.println(Numeric.toHexString(publicKey.toByteArray()));
        int recId = -1;
        for (int i = 0; i < 4; i++) {
            try {
                Class clazz = Class.forName("org.web3j.crypto.Sign");
                Method method = clazz.getDeclaredMethod("recoverFromSignature", int.class, ECDSASignature.class, byte[].class);
                method.setAccessible(true);
                BigInteger k = (BigInteger) method.invoke(null, i, sig, messageHash);
                if (k != null && k.equals(publicKey)) {
                    recId = i;
                    break;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
//            BigInteger k = Sign.recoverFromSignature(i, sig, messageHash);
//            if (k != null && k.equals(publicKey)) {
//                recId = i;
//                break;
//            }
        }
//        for (int i = 0; i < 4; i++) {
//            BigInteger k = Sign.recoverFromSignature(i, sig, messageHash);
//            if (k != null && k.equals(publicKey)) {
//                recId = i;
//                break;
//            }
//        }
        if (recId == -1) {
//            throw new RuntimeException(
//                    "Could not construct a recoverable key. This should never happen.");
            Log.e("TEmpActivity", "Could not construct a recoverable key. This should never happen.");
        }

        int headerByte = recId + 27;

        // 1 header + 32 bytes for R + 32 bytes for S
        byte v = (byte) headerByte;
        byte[] r = Numeric.toBytesPadded(sig.r, 32);
        byte[] s = Numeric.toBytesPadded(sig.s, 32);

        Sign.SignatureData signatureData=new Sign.SignatureData(v, r, s);
        byte[] signedMessage = TransactionEncoder.encode(etherTransaction, signatureData);
        //----签名结束


        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));

        // ECPrivateKeyParameters privKey = new ECPublicKeyParameters(ke.getPublicKey(), CURVE);
        //signer.init(false, privKey);
        // BigInteger[] components = signer.verifySignature(sig,Numeric.toBigInt(signatureData.getR()),Numeric.toBigInt(signatureData.getS()));




        //这是自带的签名,用来比对
        byte[] bytes = TransactionEncoder.signMessage(etherTransaction, ke);




        System.out.println(Numeric.toHexString(bytes));
        System.out.println(Numeric.toHexString(signedMessage));
    }

    void writeKey() {
        JNIAPI jniapi = new JNIAPI();//加载JAR包接口类
        int ret = 0;
        int[] devNum = {0};
        long[] handle = {0};
        byte[] dataIn = new byte[32];//待签名数据必须经过HASH运算，长度是32，测试程序用32个0x01模拟
        byte[] signdata = new byte[64];
        int[] outlen = {0};
        for (int i=0;i<32;i++)
        {
            dataIn[i] = 0x01;
        }
        //枚举卡
        ret = jniapi.EnumDev(jniapi.CT_ALL,devNum);
        if (ret != JNIAPI.XKR_OK  || 0 == devNum[0])
        {
            //未找到卡，出错
            return;
        }
        //打开卡
        ret = jniapi.OpenDev(0,handle);
        if (JNIAPI.XKR_OK != ret)
        {
            //打开卡出错
            return;
        }
        //修改PIN
        ret = jniapi.ChangePIN(handle[0],jniapi.ROLE_A,"111111".getBytes(),6,"222222".getBytes(),6);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            return;
        }
        //认证PIN
        ret = jniapi.VerifyPIN(handle[0],jniapi.ROLE_A,"222222".getBytes(),6);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            return;
        }
        byte[] pubid = new byte[2];
        byte[] priid = new byte[2];
        XDJA_SM2_PUBKEY ECCpub = new XDJA_SM2_PUBKEY();
        XDJA_SM2_PRIKEY ECCPri = new XDJA_SM2_PRIKEY();
        ret = jniapi.GenECDSAKeyPair(handle[0],pubid,priid,ECCpub,ECCPri);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            return;
        }
        pubid[1] = 0x2a;
        priid[1] = 0x2b;
        ret = jniapi.WriteSm2PubKey(handle[0],pubid,ECCpub);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            return;
        }
        ret = jniapi.WriteSm2PriKey(handle[0],priid,ECCPri);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            return;
        }
        ret = jniapi.ECDSASign(handle[0],pubid,priid,dataIn,signdata,outlen);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            return;
        }
        ret = jniapi.ECDSASignVerify(handle[0],pubid,null,dataIn,signdata);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            return;
        }
        jniapi.CloseDev(handle[0]);//关闭卡
    }

    public void test3(){
        log("开始操作卡");
        JNIAPI jniapi = new JNIAPI();//加载JAR包接口类
        int ret = 0;
        int[] devNum = {0};
        long[] handle = {0};
        byte[] dataIn = new byte[32];//待签名数据必须经过HASH运算，长度是32，测试程序用32个0x01模拟
        byte[] signdata = new byte[64];
        int[] outlen = {0};
        for (int i=0;i<32;i++)
        {
            dataIn[i] = 0x01;
        }
        log("dataIn = " + Numeric.toHexString(dataIn));
        //枚举卡
        ret = jniapi.EnumDev(jniapi.CT_ALL,devNum);
        log("devNum = " + devNum.length);
        if (ret != JNIAPI.XKR_OK  || 0 == devNum[0])
        {
            //未找到卡，出错
            log("未找到卡，出错");
            return;
        }
        //打开卡
        ret = jniapi.OpenDev(0,handle);
        if (JNIAPI.XKR_OK != ret)
        {
            //打开卡出错
            log("打开卡出错");
            return;
        }
        //验证PIN
        ret = jniapi.VerifyPIN(handle[0],jniapi.ROLE_A,"111111".getBytes(),6);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            log("出错，返回ret错误码, code = " + ret);
            return;
        }
        //产生
//         pubid = new byte[2];
//         priid = new byte[2];
        XDJA_SM2_PUBKEY ECCpub = new XDJA_SM2_PUBKEY();
        XDJA_SM2_PRIKEY ECCPri = new XDJA_SM2_PRIKEY();
//        for (int i=0;i<32;i++)
//        {
//            ECCPri.d[i] = (byte) count;
//        }

        ECCPri.d = Numeric.hexStringToByteArray("393718e110a67020cdbbf8b7f44fe053e541b1ac3a12d8e378be26469be64518");
        ECCpub.x = Numeric.hexStringToByteArray("67ac7e2de855bf87a01fc84b2c8dc714f09f2365e74c42d243349ad2ffeb49ad0ba8cbcfe24f3b219c199e024ef4a80c0e6c3cc5a6f840809704831b1c4c5420".substring(0,64));
        ECCpub.y = Numeric.hexStringToByteArray("67ac7e2de855bf87a01fc84b2c8dc714f09f2365e74c42d243349ad2ffeb49ad0ba8cbcfe24f3b219c199e024ef4a80c0e6c3cc5a6f840809704831b1c4c5420".substring(64,128));
        ret = jniapi.GenECDSAKeyPair(handle[0],pubid,priid,ECCpub,ECCPri);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            log("GenECDSAKeyPair 出错，返回ret错误码, code = " + ret);
            return;
        }
        log("产生 pubid = " + Numeric.toHexString(pubid));
        log("产生 priid = " + Numeric.toHexString(priid));
        log("产生 ECCpub ECCpub.x = " + Numeric.toHexString(ECCpub.x) + "  ECCpub.y = " + Numeric.toHexString(ECCpub.y));
        log("产生 ECCPri = " + Numeric.toHexString(ECCPri.d));
        pubid[1] = 0x2a;
        priid[1] = 0x2b;

        ret = jniapi.WriteSm2PubKey(handle[0],pubid,ECCpub);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            log("WriteSm2PubKey 出错，返回ret错误码, code = " + ret);
            return;
        }
        ret = jniapi.WriteSm2PriKey(handle[0],priid,ECCPri);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            log("WriteSm2PriKey 出错，返回ret错误码, code = " + ret);
            return;
        }
        log("产生2 pubid = " + Numeric.toHexString(pubid));
        log("产生2 priid = " + Numeric.toHexString(priid));
        log("产生2 ECCpub ECCpub.x = " + Numeric.toHexString(ECCpub.x) + "  ECCpub.y = " + Numeric.toHexString(ECCpub.y));
        log("产生2 ECCPri = " + Numeric.toHexString(ECCPri.d));

        ret = jniapi.ECDSASign(handle[0],pubid,priid,dataIn,signdata,outlen);
        log("signData = " + Numeric.toHexString(signdata));
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            log("ECDSASign 出错，返回ret错误码, code = " + ret);
//            String signData = "";
//            for(byte b:signdata) {
//                signData += b;
//            }
            return;
        }
        log("outlen = " + outlen[0]);
        ret = jniapi.ECDSASign(handle[0],pubid,priid,dataIn,signdata,outlen);
        log("signData = " + Numeric.toHexString(signdata));
        ret = jniapi.ECDSASignVerify(handle[0],pubid,null,dataIn,signdata);
        if (JNIAPI.XKR_OK != ret)
        {
            //出错，返回ret错误码
            log("ECDSASignVerify 出错，返回ret错误码, code = " + ret);
//            String signData = "";
//            for(byte b:signdata) {
//                signData += b;
//            }
            return;
        }
        log("signData = " + Numeric.toHexString(signdata));
        jniapi.CloseDev(handle[0]);//关闭卡
        log("成功！关闭卡");
//        String signData = "";
//        for(byte b:signdata) {
//            signData += b;
//        }
//        tv_msg.setText(Numeric.toHexString(signdata));
    }
    byte[] pubid = new byte[2];
    byte[] priid = new byte[2];

    void log(String msg) {
        Log.d(getClass().getName(), msg);
    }
}
