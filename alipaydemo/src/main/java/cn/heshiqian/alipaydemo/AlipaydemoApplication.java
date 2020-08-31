package cn.heshiqian.alipaydemo;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import lombok.Data;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@SpringBootApplication
//开启配置文件注入
@EnableConfigurationProperties
public class AlipaydemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlipaydemoApplication.class, args);
	}

	/**
	 * 程序启动完毕后，自动初始化
	 */
	@Configuration
	public static class ApplicationInit implements ApplicationRunner{
		@Resource
		Config config;
		@Override
		public void run(ApplicationArguments args) throws Exception {
			Factory.setOptions(config);
		}
	}

	/**
	 * 映射配置文件
	 * 创建Config Bean
	 * {@link com.alipay.easysdk.kernel.Config}
	 */
	@Component
	@PropertySource(value = {"classpath:pay.properties"}, name = "pay.properties")
	@ConfigurationProperties(prefix = "alipay")
	@Data
	public static class alipayConfig{

		private String gatewayHost;
		private String appId;
		private String privateCode;
		private String alipayPublicKey;
		private String notifyUrl;

		@Bean
		public Config buildConfig(){
			Config config = new Config();
			config.gatewayHost = gatewayHost;
			config.alipayPublicKey = alipayPublicKey;
			config.signType="RSA2";
			config.protocol="https";
			config.appId=appId;
			config.merchantPrivateKey=privateCode;
//			异步暂时不用，需要公网IP
//			config.notifyUrl=notifyUrl;
			return config;
		}
	}


}
