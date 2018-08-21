package studio.jedjiang.license;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.hutool.core.codec.Base64;

public class LicenseUtil {
	
	public final static int LICENSE_EXPIRED = 1002;
	
	// 系统启动会加载license后读取的到期时间写入内存
	public static long plain_end_date = 0;
	
	public final static String pk = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQClDXExPH/Tgdb0RDf+IFN6CK0jD2TDg7rD/KMuPGrJs2CfT/ayv89GwUK1W1c2mx2vGMVqLv/1E8CwVYg/9S+csA+Nr7TksTeFhImr5ZHmOJjtUgDd1ikurQqaggjO9H6ecUvED81SXnlxyGn+T83G6cWgVNPg5urEYZ272S7kAQIDAQAB";
	public final static String pvk = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKUNcTE8f9OB1vREN/4gU3oIrSMPZMODusP8oy48asmzYJ9P9rK/z0bBQrVbVzabHa8YxWou//UTwLBViD/1L5ywD42vtOSxN4WEiavlkeY4mO1SAN3WKS6tCpqCCM70fp5xS8QPzVJeeXHIaf5PzcbpxaBU0+Dm6sRhnbvZLuQBAgMBAAECgYB7aOObtuq00L5SgbeEEdOYyWf7cQUckyBQblPsvEv0aGT0eaux1206MEKaq6M4Nx8oztK9JEtJie1na2ymtcNUp17UG9n8XruWE37tctKD0XbKgP/JaGnImbX2gNXidaEnuOsd+PqLfxTKSiZIYeUuivkZGcCd40NWr5BrUyuwyQJBANPj9SgYV8EB0gEP0/8DADZHaLabLJEbElkqwwZor5dpCQ0RLMcnP6qkWyNMsiznB65nh8kxg26ajChUNyL75AMCQQDHaWbX4yPKcEAtQntj85PPdk65lOLuQLUr9T0/ZeLOa/m/3mYSEr74P31GtXJLJBk+0UUX+GEmA58dzT0ZdzKrAkB3j8Wh3jqGGfTZzIie+J6CAkE/6Md5CYGM9ugdEa0jDo2g5fQSXD88lokmnfxcAfD/pEOmb13uZx53Ey4Ze3DpAkB6OHf4ShddtH8hPmYyVNB7j5+gz0hnjvF8B8uKWAAzVKq2lrEo0qSJsyAtfniW5YEzp0AZ+PpSOlYEBg801IIRAkADGqtparLMNnOoiKrwUREXKELMLfdcEh4HxAzRsCUM3KprPitig7uLPLy28p01Aqkg0kQacH9xo8X2ecmZdP4B";
	
	public static String createLicense(Date endDate) throws Exception {
		long data = endDate.getTime();
		byte[] encData = RSAUtil.operByPublicKey(String.valueOf(data).getBytes(), pk, RSAUtil.ENCRYPT_MODE);
		return Base64.encode(encData);
	}
	
	public static void readLocalLicense() throws Exception{
		// 从工程相对目录读取license文件信息
		plain_end_date = Long.valueOf("1536940800000");
		boolean expired = checkIfExpired();
		if(expired) {
			throw new Exception("授权服务已到期");
		}
	}
	
	// 返回true说明license过期
	public static boolean checkIfExpired() {
		long now = new Date().getTime();
		return now > LicenseUtil.plain_end_date;
	}
	
	public static long extractLicense(String encLicense) throws Exception {
		byte[] byteLicense = Base64.decode(encLicense);
		byte[] decodeData = RSAUtil.operByPrivateKey(byteLicense, pvk, RSAUtil.DECRYPT_MODE);
		String decDate = new String(decodeData);
		return Long.parseLong(decDate);
	}
	
	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	
	public static void main(String[] args) throws Exception {
		String licesnedate = "2018-09-15";
		String encLicense = createLicense(dateFormat.parse(licesnedate));
		System.out.printf("生成license：%s", encLicense);
		System.out.println();
		System.out.printf("解密license %d", extractLicense(encLicense));
	}

}
