package studio.jedjiang.license;

import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.UTF8JsonView;

import studio.jedjiang.bean.Result;
/**
 * license服务到期，给页面报1002 error
 * @author Jed
 *
 */
public class LicenseProcessor implements ActionFilter{

	@Override
	public View match(ActionContext ctx) {
		if(LicenseUtil.checkIfExpired()) {
			Result result = Result.NEW().addCode(LicenseUtil.LICENSE_EXPIRED).addMsg("授权到期");
			return new UTF8JsonView().setData(result);
		}
		return null;
	}

}
