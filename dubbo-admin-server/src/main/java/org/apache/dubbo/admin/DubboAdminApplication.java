/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dubbo.admin;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude={
		HibernateJpaAutoConfiguration.class
})
@EnableDubbo(scanBasePackages = {"org.apache.dubbo.admin.provider"})
@MapperScan(basePackages = {"org.apache.dubbo.admin.mapper"})
public class DubboAdminApplication {

	public static void main(String[] args) {
		// 安全加固兜底：本项目 fastjson1 已整体升级为 fastjson2（见根 pom fastjson2-version），
		// 此处预设 fastjson1 的 safeMode 开关，防止未来依赖变动把 fastjson1(<=1.2.83) 经由传递依赖混入
		// 后再次暴露 autoType 反序列化漏洞；fastjson2 不读取该属性，设置它没有任何副作用。
		System.setProperty("fastjson.parser.safeMode", "true");
		SpringApplication.run(DubboAdminApplication.class, args);
	}
}
