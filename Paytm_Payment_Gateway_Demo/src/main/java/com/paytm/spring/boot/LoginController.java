package com.paytm.spring.boot;

import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.paytm.bean.LoginBean;
import com.paytm.model.Paytm;
import com.paytm.pg.merchant.CheckSumServiceHelper;

@Controller
public class LoginController {
	@RequestMapping(value = "login", method = RequestMethod.GET)
	public String init(Model model) {
		model.addAttribute("msg", "Please Enter Your Login Details");
		return "login.jsp";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String submit(Model model,
			@ModelAttribute("loginBean") LoginBean loginBean) {
		if (loginBean != null && loginBean.getUserName() != null
				& loginBean.getPassword() != null) {
			if (loginBean.getUserName().equals("vaibhav")
					&& loginBean.getPassword().equals("vaibhav123")) {
				model.addAttribute("msg", loginBean.getUserName());
				return "success.jsp";
			} else {
				model.addAttribute("error", "Invalid Details");
				return "login.jsp";
			}
		} else {
			model.addAttribute("error", "Please enter Details");
			return "login.jsp";
		}
	}
	
	@RequestMapping(value = "/paytm", method = RequestMethod.GET)
    public ModelAndView paytm(){

        return new ModelAndView("TxnTest.jsp");
    }

    @RequestMapping(value = "/paytmResponseURL", method = RequestMethod.POST)
    public ModelAndView paytmResponseURL(@ModelAttribute("command")Paytm paytm){
        Paytm paytm1 = new Paytm();
        Map<String, Object> map = new HashedMap<>();
        map.put("paytm", paytm);

        return new ModelAndView("pgRedirect.jsp",map);
    }

    @RequestMapping(value = "/pgResponse", method = RequestMethod.POST)
    public ModelAndView pgResponse(HttpServletRequest request){
        Paytm paytm1 = new Paytm();
        Map<String, Object> map = new HashedMap<>();
        String msg = request.getParameter("RESPMSG");
        map.put("message", msg);	
        Enumeration<String> paramNames = request.getParameterNames();

        Map<String, String[]> mapData = request.getParameterMap();
        TreeMap<String,String> parameters = new TreeMap<String,String>();
        String paytmChecksum =  "";
        while(paramNames.hasMoreElements()) {
            String paramName = (String)paramNames.nextElement();
            if(paramName.equals("CHECKSUMHASH")){
                paytmChecksum = mapData.get(paramName)[0];
            }else{
                parameters.put(paramName,mapData.get(paramName)[0]);
            }
        }


        boolean isValideChecksum = false;
        String outputHTML="";
        try{
            isValideChecksum = CheckSumServiceHelper.getCheckSumServiceHelper().verifycheckSum("gKpu7IKaLSbkchFS",parameters,paytmChecksum);
            if(isValideChecksum && parameters.containsKey("RESPCODE")){
                if(parameters.get("RESPCODE").equals("01")){
                    outputHTML = parameters.toString();
                }else{
                    outputHTML="<b>Payment Failed.</b>";
                }
            }else{
                outputHTML="<b>Checksum mismatched.</b>";
            }
        }catch(Exception e){
            outputHTML=e.toString();
        }
        return new ModelAndView("pgResponse.jsp",map);
    }
}
