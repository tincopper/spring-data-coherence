# spring-data-coherence
这是一个使用spring集成coherence缓存的应用工具，提供了非常方便操作coherence的api


# spring-data-coherence
2
这是一个使用spring集成coherence缓存的应用工具，提供了非常方便操作coherence的api

# 如何使用
1. spring集成spring-data-coherence
只需在配置文件添加如下配置即可：
```xml
<bean id="coherenceTemplate" class="com.tomgs.spring.data.coherence.support.CoherenceTemplate"/>
```
2. 配置序列化实体
配置文件可以参考如下路径：
```
spring-data-coherence/src/main/resources/seovic-coherence-pof-config.xml
```

里面同时也定义了一些默认的POF文件：

可以在pof配置信息xml文件中查看`cache-pof-config-message.xml`，这个文件一般需要在项目中覆盖，然后在里面添加自己定义的pof配置文件如`test-coherence-pof-config.xml`

```xml
<!-- cache-pof-config-message.xml示例 -->

<?xml version="1.0"?>
<pof-config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://xmlns.oracle.com/coherence/coherence-pof-config"
	xsi:schemaLocation="http://xmlns.oracle.com/coherence/coherence-pof-config coherence-pof-config.xsd">
	<user-type-list>
	   <!-- Coherence 内部POF定义 -->
		<include>coherence-pof-config.xml</include>
		<!-- Coherence 序列生成器POF -->
		<include>coherence-common-pof-config.xml</include>
	    <!-- seovic POF定义 支持PortableSet-->
		<include>seovic-coherence-pof-config.xml</include>
	     <!-- 应用 POF定义 -->
		<include>test-coherence-pof-config.xml</include>
	</user-type-list>
</pof-config>
```
> 如果不需要上述的功能可以去掉对应的<include>标签，建议保留

> 这些配置文件可以通过项目启动时指定系统参数进行指定。如使用自定义的POF配置文件-Dtangosol.pof.config=cache-pof-config-message.xml

3. 实体POF序列化示例

```java
public class PayUserDataSerializer implements PofSerializer {

	private static final int ID = 0;
	private static final int USER_NAME = 1;
	private static final int CUSTOMER_ID = 2;
	private static final int PHONE = 3;
	private static final int DEFAULT_ACCOUNT_ID = 4;
	private static final int ACCOUNTS = 5;
	private static final int SMSREMIND_FLAG = 6;
	private static final int SMSREMIND_PHONE = 7;
	private static final int LOGIN_PWD = 8;
	private static final int PAY_PWD = 9;
	private static final int QQ = 10;
	private static final int EMAIL = 11;
	
	@Override
	public PayUserData deserialize(PofReader reader) throws IOException {
		
		PayUserData data = new PayUserData();
		
		data.setId(reader.readString(ID));
		data.setUserName(reader.readString(USER_NAME));
		data.setCustomerId(reader.readString(CUSTOMER_ID));
		data.setPhone(reader.readString(PHONE));
		data.setDefaultAccountId(reader.readString(DEFAULT_ACCOUNT_ID));
		data.setAccounts((List<PayAccountData>)reader.readCollection(ACCOUNTS, data.getAccounts()));
		data.setSmsRemindFlag(reader.readBoolean(SMSREMIND_FLAG));
		data.setSmsRemindPhone(reader.readString(SMSREMIND_PHONE));
		data.setLoginPwd(reader.readString(LOGIN_PWD));
		data.setPayPwd(reader.readString(PAY_PWD));
		data.setQq(reader.readString(QQ));
		data.setEmail(reader.readString(EMAIL));
		
		reader.readRemainder();
		return data;
	}

	public void serialize(PofWriter writer, PayUserData data) throws IOException {
		
		writer.writeString(ID, data.getId());
		writer.writeString(USER_NAME, data.getUserName());
		writer.writeString(CUSTOMER_ID, data.getCustomerId());
		writer.writeString(PHONE, data.getPhone());
		writer.writeString(DEFAULT_ACCOUNT_ID, data.getDefaultAccountId());
		writer.writeCollection(ACCOUNTS, data.getAccounts());
		writer.writeBoolean(SMSREMIND_FLAG, data.getSmsRemindFlag());
		writer.writeString(SMSREMIND_PHONE, data.getSmsRemindPhone());
		writer.writeString(LOGIN_PWD, data.getLoginPwd());
		writer.writeString(PAY_PWD, data.getPayPwd());
		writer.writeString(QQ, data.getQq());
		writer.writeString(EMAIL, data.getEmail());
		
		writer.writeRemainder(null);
	}
	@Override
	public void serialize(PofWriter writer, Object obj) throws IOException {
		serialize(writer, (PayUserData)obj);
	}
}
```

4、使用示例

`CoherenceBaseTest.java`
```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:application-cache-coherence.xml")
public class CoherenceBaseTest extends AbstractJUnit4SpringContextTests {

}

```

`CoherenceTemplateTest.java`
```java
public class CoherenceTemplateTest extends CoherenceBaseTest {
	
	@Resource(name = "coherenceTemplate")
	private CoherenceTemplate<String, Object> coherenceTemplate;
	
	String cacheName1 = "cache-pc-test1";
	String cacheName2 = "cache-pc-test2";
	
	@Test
	public void testPut1() {
		
		String k = "2";
		Object v = "tincopper";
		Object result = coherenceTemplate.opsForValue(cacheName1).put(k, v);
		System.out.println(result);
		System.out.println(coherenceTemplate.opsForValue(cacheName1).get(k));
		
		k = "3";
		v = "tomgs";
		result = coherenceTemplate.opsForValue(cacheName2).put(k, v);
		System.out.println(result);
		System.out.println(coherenceTemplate.opsForValue(cacheName2).get(k));
		
	}
}

```
