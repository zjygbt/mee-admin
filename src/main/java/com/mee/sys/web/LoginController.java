package com.mee.sys.web;
import com.mee.common.service.ShiroAccountLockedServiceImpl;
import com.mee.core.configuration.ShiroUtils;
import com.mee.sys.entity.SysUser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

import javax.annotation.Resource;

/**
 * @author funnyzpc
 * 主页面
 */
@Controller
public class LoginController {
	private static final Logger log = LoggerFactory.getLogger(LoginController.class);

	@Resource
	private ShiroAccountLockedServiceImpl shiroAccountLockedService;

	@GetMapping("/login")
	public String index(){
		Subject subject = SecurityUtils.getSubject();
		if(subject.isAuthenticated()){
			shiroAccountLockedService.clearCounter(ShiroUtils.getLoginName());
			return "redirect:main";
		}
		return "login";
	}

	/** 认证不通过走这里 **/
	@PostMapping("/login")
	public String login(Model m, @RequestAttribute("shiroLoginFailure") String shiroLoginFailure, SysUser sysUser){
		log.error("登录失败：{}->{}",shiroLoginFailure,sysUser);
		// 错误类型请见：org.apache.shiro.authc.
		switch (shiroLoginFailure){
			case "org.apache.shiro.authc.UnknownAccountException":
				m.addAttribute("msg","用户不存在");break;
			case "org.apache.shiro.authc.IncorrectCredentialsException":
				shiroAccountLockedService.addOne(sysUser.getUsername());
				m.addAttribute("msg","密码错误");break;
			case "org.apache.shiro.authc.LockedAccountException":
				// shiroAccountLockedService.addOne(sysUser.getUsername());
				m.addAttribute("msg","重试次数过,多账户已锁定,请联系管理员~");break;
			case "org.apache.shiro.authc.DisabledAccountException":
				shiroAccountLockedService.addOne(sysUser.getUsername());
				m.addAttribute("msg","账户不可用");break;
			default:
				m.addAttribute("msg","未知错误请联系管理员:"+shiroLoginFailure);
				break;
		}
		return "login";
	}

	
	@GetMapping("/logout")
	public String logout(){
		// ServiceContext.getInstance().setUser(null);
		// 退出前要清空
		Subject subject = SecurityUtils.getSubject();
		subject.logout();
		return "redirect:login";
	}

}
