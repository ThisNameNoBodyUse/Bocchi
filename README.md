# Bocchi
Bocchi Restaurant, based on Spring Cloud Alibaba, integrates microservices like Sentinel, Gateway, Nacos, Feign, and Seata. The platform incorporates Redis distributed locks and RabbitMQ message queues for concurrency processing, uses a dual-token mechanism for seamless token refreshing, and enhances system throughput with efficient caching.

请注意以下配置调整：
  将EmailUtils类中的邮箱配置修改为您自己的邮箱。
  各微服务的端口、IP地址、Redis、Nacos等配置修改为您实际使用的配置。
  将OSS配置修改为您的实际配置。
Please note the following configuration adjustments:
  Change the email configuration in the EmailUtils class to your own email.
  Update the ports, IP addresses, Redis, Nacos, and other configurations of each microservice to your actual configurations.
  Modify the OSS configuration to match your actual setup.
