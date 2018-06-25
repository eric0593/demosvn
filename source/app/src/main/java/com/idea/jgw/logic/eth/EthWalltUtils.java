package com.idea.jgw.logic.eth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idea.jgw.App;
import com.idea.jgw.common.Common;
import com.idea.jgw.logic.eth.data.FullWallet;
import com.idea.jgw.logic.eth.data.WalletDisplay;
import com.idea.jgw.logic.eth.network.EtherscanAPI;
import com.idea.jgw.logic.eth.service.TransactionService;
import com.idea.jgw.logic.eth.utils.AddressNameConverter;
import com.idea.jgw.logic.eth.utils.KeyStoreUtils;
import com.idea.jgw.logic.eth.utils.ResponseParser;
import com.idea.jgw.logic.eth.utils.WalletStorage;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.MyLog;

import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.web3j.crypto.Keys.ADDRESS_LENGTH_IN_HEX;

/**
 * 以太钱包工具类
 * （总入口  createWallet()方法）
 * Created by vam on 2018\6\1 0001.
 */

public class EthWalltUtils extends WalletUtils {

    /**
     * 发送币(上层需自己判断金额够不 getCurAvailable（....）)
     *
     * @param fromAddress    从那里转出
     * @param toAddress      转给谁
     * @param password       密码
     * @param sendAmountGwei 转账金额（单位gwei）
     * @param gasPrice       手续费（单位gwei）
     * @param gasLimit       最大限制（单位gwei）
     */
    public static void sendCoin(Activity ac, String fromAddress, String toAddress, String password, String sendAmountGwei, long gasPrice, int gasLimit) {
        if (TextUtils.isEmpty(fromAddress) || !EthWalltUtils.isValidAddress(fromAddress)) {

        } else if (TextUtils.isEmpty(toAddress) || !EthWalltUtils.isValidAddress(toAddress)) {

        }
//        else if (sendAmountGwei == 0 || sendAmountGwei < 0) {
//
//        }
//        else if (gas == 0 || gas < 0) {
//            BigDecimal curTxCost = new BigDecimal("0.000252");
//        }
        else {

            BigInteger gasLimitInt = null;
            if (gasLimit <= 0) {
                gasLimitInt = new BigInteger("21000");
            } else {
                try {
                    gasLimitInt = new BigInteger(String.valueOf(gasLimit));
                } catch (Exception e) {
                    gasLimitInt = new BigInteger("21000");
                }
            }

            Intent txService = new Intent(ac, TransactionService.class);
            txService.putExtra("FROM_ADDRESS", fromAddress);
            txService.putExtra("TO_ADDRESS", toAddress);
            txService.putExtra("AMOUNT", sendAmountGwei); // In ether, gets converted by the service itself //在以太网中，由服务本身进行转换
            txService.putExtra("GAS_PRICE", String.valueOf(gasPrice));// (new BigDecimal(realGas + "").multiply(new BigDecimal("1000000000")).toBigInteger()).toString());// "21000000000");
            txService.putExtra("GAS_LIMIT", gasLimitInt.toString());
            txService.putExtra("PASSWORD", password);
            txService.putExtra("DATA", "");
            ac.startService(txService);

//            //金额不足
//           double  realGas = (gas - 8);
//            if (gas < 10)
//                realGas = (double) (gas + 1) / 10d;
//             BigDecimal curTxCost = new BigDecimal("0.000252");
//            BigInteger gaslimit = new BigInteger("21000");
//            curTxCost = (new BigDecimal(gaslimit).multiply(new BigDecimal(realGas + ""))).divide(new BigDecimal("1000000000"), 6, BigDecimal.ROUND_DOWN);
//            //
//
//            BigDecimal curAmount = BigDecimal.ZERO;
//            BigDecimal  b =  curAmount.add(curTxCost, MathContext.DECIMAL64);
//
//            if(b.compareTo(curAvailable) < 0){
//
//            }
        }
    }


    /**
     * 创建钱包的总入口（创建的是一个正常钱包）
     *
     * @param context    上下文
     * @param privatekey 私钥 ，可以为空
     * @param password   创建钱包的密码
     * @param callback   回调接口
     */
    public static void createWallet(Context context, String privatekey, String password, CreateUalletCallback callback) {
        try {
            String walletAddress;
            if (TextUtils.isEmpty(privatekey)) { // Create new key
                walletAddress = EthWalltUtils.generateNewWalletFile(password, new File(context.getFilesDir(), ""), true);
            } else { // Privatekey passed
                ECKeyPair keys = ECKeyPair.create(Hex.decode(privatekey));
                MyLog.e("keys.getPrivateKey()--->>" + keys.getPrivateKey());
                MyLog.e("keys.getPublicKey()-->>>" + keys.getPublicKey());
//                ECKeyPair keys = ECKeyPair.create(Hex.decode("f7bf7d387e9a9e576e70b3ae902731a935655dd78828224b3f5c8eb807b0ef3dd260a3c5f3a3053a7da01e0b1a075f975de05a54ba2a990b21cfebd16fb819bf"));
                walletAddress = EthWalltUtils.generateWalletFile(password, keys, new File(context.getFilesDir(), ""), true);
            }
            WalletStorage.getInstance(context).add(new FullWallet("0x" + walletAddress, walletAddress), context);
            AddressNameConverter.getInstance(context).put("0x" + walletAddress, "Wallet " + ("0x" + walletAddress).substring(0, 6), context);

            MyLog.e("adddres--->>"+walletAddress);

            SPreferencesHelper.getInstance(App.getInstance()).saveData(Common.Eth.PREFERENCES_ADDRESS_KEY, walletAddress);


            callback.onSuccess(walletAddress);
        } catch (Exception e) {
            callback.onFaild();
        }
    }

    /**
     * 获取可用的姨太币数量
     *
     * @param address  地址
     * @param callback 回调
     */
    public static final void getCurAvailable(String address, Callback callback) {
        try {
            EtherscanAPI.getInstance().getBalance(address, callback != null ? callback : new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
//                    ac.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                BigDecimal curAvailable = new BigDecimal(ResponseParser.parseBalance(response.body().string(), 6));
//                            } catch (Exception e) {
//                                ac.snackError("Cant fetch your account balance");
//                                e.printStackTrace();
//                            }
//                        }
//                    });
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            if (null != callback) {
                callback.onFailure(null, e);
            }
        }
    }

    /**
     * 删除钱包
     *
     * @param context
     * @param address
     */
    public static void delWallet(Context context, String address) {
        WalletStorage.getInstance(context).removeWallet(address, context);
    }


    /**
     * @param ethAddress 接受的地址（也就是publicKey）
     * @param amount     需要接受的数量，可以为0（单位gwei）
     */
    public static String requestCoin(String ethAddress, long amount) {
        String iban = "iban:" + ethAddress;
        if (amount > 0) {
            iban += "?amount=" + amount;
        }
        return iban;
    }

    public static interface CreateUalletCallback {
        public void onSuccess(String address);

        public void onFaild();
    }

    // OVERRIDING THOSE METHODS BECAUSE OF CUSTOM WALLET NAMING (CUTING ALL THE TIMESTAMPTS FOR INTERNAL STORAGE)

    public static String generateFullNewWalletFile(String password, File destinationDirectory) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException {
        return generateNewWalletFile(password, destinationDirectory, true);
    }

    public static String generateLightNewWalletFile(String password, File destinationDirectory) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException {

        return generateNewWalletFile(password, destinationDirectory, false);
    }

    public static String generateNewWalletFile(String password, File destinationDirectory, boolean useFullScrypt)
            throws CipherException, IOException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {
        ECKeyPair ecKeyPair = ECKeyPair.create(createSecp256k1KeyPair());// Keys.createEcKeyPair(); //createSecp256k1KeyPair() //测试

        MyLog.e("keys.getPrivateKey()--->>" + ecKeyPair.getPrivateKey());
        MyLog.e("keys.getPublicKey()-->>>" + ecKeyPair.getPublicKey());
        return generateWalletFile(password, ecKeyPair, destinationDirectory, useFullScrypt);
    }

    static KeyPair createSecp256k1KeyPair() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
//        KeyPairGenerator keyPairGenerator2 = new org.spongycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi.ECDSA();
        KeyPairGenerator keyPairGenerator = new org.spongycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi.ECDSA();// KeyPairGenerator.getInstance("ECDSA", "SC");
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");


        //遍历所有的属性，动态修改engine的具体实现类
        //  org.spongycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi.engine
        // ECKeyPairGenerator 变为ECKeyPairGenerator2
        Class<?> clazz = keyPairGenerator.getClass();
        try {
            for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    int mod = field.getModifiers();
                    if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object val = field.get(keyPairGenerator);
                    if (val instanceof org.spongycastle.crypto.generators.ECKeyPairGenerator) {
                        ECKeyPairGenerator2 ec = new ECKeyPairGenerator2();
                        field.set(keyPairGenerator, ec);
                    }
                }
            }
        } catch (Exception e) {
            MyLog.e(e.getMessage());
        }
        keyPairGenerator.initialize(ecGenParameterSpec, new SecureRandom());
        KeyPair kp = keyPairGenerator.generateKeyPair();
        BigInteger i = ECKeyPairGenerator2.shangInt;
        return kp;
    }

    public static String generateWalletFile(String password, ECKeyPair ecKeyPair, File destinationDirectory, boolean useFullScrypt) throws CipherException, IOException {

           if(true){
               if (!destinationDirectory.exists()) {
                   destinationDirectory.mkdirs();
               }
//               ECKeyPair ecKeyPair = Keys.createEcKeyPair();
               //在外置卡生成
               String filename = WalletUtils.generateWalletFile(password, ecKeyPair, destinationDirectory, false);

               KeyStoreUtils.genKeyStore2Files(ecKeyPair);

               String msg = "fileName:\n" + filename
                       + "\nprivateKey:\n" + Numeric.encodeQuantity(ecKeyPair.getPrivateKey())
                       + "\nPublicKey:\n" + Numeric.encodeQuantity(ecKeyPair.getPublicKey());
               MyLog.e("地址信息>>>",msg);

               SPreferencesHelper.getInstance(App.getInstance()).saveData(Common.Eth.PREFERENCES_PRIVET_KEY, ecKeyPair.getPrivateKey().toString());
               SPreferencesHelper.getInstance(App.getInstance()).saveData(Common.Eth.PREFERENCES_PWD_KEY, password);

               SPreferencesHelper.getInstance(App.getInstance()).saveData(Common.Eth.FILE_DIR, destinationDirectory.getPath());
               SPreferencesHelper.getInstance(App.getInstance()).saveData(Common.Eth.FILE_NAME, filename);


               int lastIndex = filename.lastIndexOf("--")+2;
               filename = filename.substring(lastIndex,filename.length() -5);
               return filename;
           }





        WalletFile walletFile;
        if (useFullScrypt) {
            walletFile = Wallet.createStandard(password, ecKeyPair);
        } else {
            walletFile = Wallet.createLight(password, ecKeyPair);
        }

        //保存私钥跟密码
        SPreferencesHelper.getInstance(App.getInstance()).saveData(Common.Eth.PREFERENCES_PRIVET_KEY, ecKeyPair.getPrivateKey().toString());
        SPreferencesHelper.getInstance(App.getInstance()).saveData(Common.Eth.PREFERENCES_PWD_KEY, password);


        String fileName = getWalletFileName(walletFile);
        File destination = new File(destinationDirectory, fileName);

        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        objectMapper.writeValue(destination, walletFile);

        return fileName;
    }

    private static String getWalletFileName(WalletFile walletFile) {
        return walletFile.getAddress();
    }


    public static boolean isValidAddress(String address) {
        String addressNoPrefix = Numeric.cleanHexPrefix(address);
        if (addressNoPrefix.contains("0x")) {
            int index = addressNoPrefix.indexOf("0x") + 2;
            addressNoPrefix = addressNoPrefix.substring(index);
        }
        return addressNoPrefix.length() == ADDRESS_LENGTH_IN_HEX;
    }
}
