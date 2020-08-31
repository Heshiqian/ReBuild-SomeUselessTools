## 一个Alipay的支付小DEMO

* 无任何安全措施，仅仅只是为了体验支付宝API配合新版本SDK的快速开发融合。
* 使用支付宝的沙箱环境，不是线上（没有资格o(╥﹏╥)o）

## 已探明BigHole

1. alipay-easysdk `2.0.2`版本中，使用工具类快捷请求时，解析响应时会抛出`NoSuchMethodError`异常，异常位置在OkHttp中response获取headers的迭代器时抛出。更换版本为`2.0.1`问题解决。

    问题在GitHub上有Issues，不过好像都没解决（迷惑行为）。

    https://github.com/alipay/alipay-easysdk/issues/115

2. 新版本API，文档中说的AlipaySignature.rsaCheckV1方法替换成了Factory.Payment.Common.verify

  目前本文版本下，方法叫`Factory.Payment.Common.verifyNotify`

  他们都是对于异步接口验证签名有用，同步返回的签名怎么验怎么都不行，可能是只能验证异步吧。

  https://opendocs.alipay.com/open/00y8k9
  https://opendocs.alipay.com/open/270/105902#%E5%BC%82%E6%AD%A5%E8%BF%94%E5%9B%9E%E7%BB%93%E6%9E%9C%E7%9A%84%E9%AA%8C%E7%AD%BE