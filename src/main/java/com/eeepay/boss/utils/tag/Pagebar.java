package com.eeepay.boss.utils.tag;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * 一个自定义的简单分页工具
 * 
 *
 */
@SuppressWarnings("serial")
public class Pagebar extends BodyTagSupport {

	/**
	 * 总页数
	 */
	private int total;

	/**
	 * 当前页码
	 */
	private int current;

	/**
	 * 需要忽略的参数
	 */
	private String skipParams;
	
	/**
	 * 分页名称
	 * 
	 * ?p=1, ?p=2
	 * 默认为 p, 如果单个页面有多个 pagebar 需要放置,
	 * 每个都有自己的分页名称, 需要设置改参数
	 */
	private String p = "p";

	/**
	 * 锚定名称，用于分页后页面保持锚定位置
	 */
	private String anchor;
	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public int doStartTag() throws JspException {
		ServletRequest req = pageContext.getRequest();
		Enumeration<String> paramsName = req.getParameterNames();

		String[] skips = null;
		if (skipParams != null) {
			skips = skipParams.split(",");
			Arrays.sort(skips);
		}

		StringBuilder sb = new StringBuilder();
		while (paramsName.hasMoreElements()) {
			String param = paramsName.nextElement();
			if ("p".equalsIgnoreCase(param)) {
				continue;
			}
			// skip
			if (skips != null) {
				if (Arrays.binarySearch(skips, param) >= 0) {
					continue;
				}
			}
			String[] vs = req.getParameterValues(param);
			for (String v : vs) {
				sb.append("&");
				sb.append(param).append("=").append(v);
			}
		}
		String params = sb.length() > 0 ? sb.toString() : null;

		JspWriter out = pageContext.getOut();
		try {
			if (current > total || current <= 0 || total <= 0) {
				out.append("<div class=\"pagebar\">\n");
				out.append("\t<span class=\"error\"><!--页码错误--></span>\n");
				return super.doStartTag();
			}

			out.append("<div class=\"pagebar\">\n");
			out.append("\t<span class=\"pages\">");
			out.append("第").append(Integer.toString(current)).append("页/共").append(Integer.toString(total)).append("页");
			out.append("</span>\n");

			if (total  == 1) {
				return super.doStartTag();
			}

			if (current != 1) {
				out.append("\t<a href=\"").append(buildLink(1, params)).append("\" class=\"first page\" title=\"第一页\">第一页</a>\n");
			} else {
				out.append("\t<span class=\"disabled\"").append(buildLink(1, params)).append("\" class=\"first page\" title=\"第一页\">第一页</span>\n");
			}

			if (current > 1) {
				out.append("\t<a href=\"").append(buildLink(current - 1, params)).append("\" class=\"prev page\" title=\"上一页\">上一页</a>\n");
			} else {
				out.append("\t<span class=\"disabled\"").append(buildLink(current - 1, params)).append("\" class=\"prev page\" title=\"上一页\">上一页</span>\n");
			}

			if (total > 10) {

				int g = current % 10;

				int next = 10;
				if (g > 5) {
					next = current - g + 20;
				} else {
					next = current - g + 10;
				}

				int prev = 0;
				if (g > 5) {
					prev = current - g - 10;
				} else {
					prev = current - g - 20;
				}

				if (prev < 0) {
					prev = 0;
				}

				if (current >= 1 && current < 4) {
					appendLinks(out, params, 1, 5);
					appendExtend(out);
					if (total > 10) {
						appendLinks(out, params, 10, 10);
						appendExtend(out);
					}
				} else if (current >= 4 && current < total - 3) {
					if (prev > 0) {
						if (prev > 1) {
							appendExtend(out);
						}
						appendLinks(out, params, prev, prev);
					}

					appendExtend(out);
					appendLinks(out, params, current - 2, current + 2);
					appendExtend(out);

					if (next < total) {
						appendLinks(out, params, next, next);
						appendExtend(out);
					}
				} else {
					if (prev > 1) {
						appendExtend(out);
					}
					if (prev == 0) {
						prev = 1;
					}
					appendLinks(out, params, prev, prev);
					appendExtend(out);
					appendLinks(out, params, current - 3, total);
				}
			} else {
				appendLinks(out, params, 1, total);
			}

			if (current < total) {
				out.append("\t<a href=\"").append(buildLink(current + 1, params)).append("\" class=\"prev page\" title=\"下一页\">下一页</a>");
			} else {
				out.append("\t<span class=\"disabled\"").append(buildLink(current + 1, params)).append("\" class=\"prev page\" title=\"下一页\">下一页</span>");
			}

			if (current != total) {
				out.append("\t<a href=\"").append(buildLink(total, params)).append("\" class=\"last page\" title=\"最后一页\">最后一页</a>\n");
			} else {
				out.append("\t<span class=\"disabled\"").append(buildLink(total, params)).append("\" class=\"last page\" title=\"最后一页\">最后一页</span>\n");
			}
		} catch (IOException e) {
			throw new JspException(e);
		}
		return super.doStartTag();
	}

	/**
	 * @param out
	 * @throws IOException 
	 */
	private void appendExtend(JspWriter out) throws IOException {
		out.append("\t<span class=\"extend\">...</span>\n");
	}

	/**
	 * @param i
	 * @param params
	 * @return
	 */
	private CharSequence buildLink(int i, String params) {
		StringBuilder sb = new StringBuilder();
		sb.append("?").append(p).append("=").append(i);
		if (params != null) {
			sb.append(params);
		}
		if(anchor!=null){
			sb.append("#"+anchor);
		}
		return sb.toString();
	}

	/**
	 * @param out
	 * @param string
	 * @param end 
	 * @param start 
	 * @throws IOException 
	 */
	private void appendLinks(JspWriter out, String link, int start, int end) throws IOException {
		for (int i = start; i <= end; i++) {
			String page = Integer.toString(i);
			if (i == current) {
				out.append("\t<span class=\"current\" title=\"第")
				.append(page).append("页\">").append(page).append("</span>\n");
			} else {
				out.append("\t<a href=\"").append(buildLink(i, link)).append("\" class=\"page\" title=\"第")
				.append(page).append("页\">").append(page).append("</a>\n");
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
	 */
	@Override
	public int doEndTag() throws JspException {
		JspWriter out = this.pageContext.getOut();
		try {
			out.append("\n</div>");
		} catch (IOException e) {
			throw new JspException(e);
		}
		return super.doEndTag();
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public void setSkipParams(String skipParams) {
		if (skipParams != null) {
			skipParams = skipParams.trim();
		}
		if (skipParams.length() > 0) {
			this.skipParams = skipParams;
		} else {
			this.skipParams = null;
		}
	}

	/**
	 * @param p set p
	 */
	public void setP(String p) {
		if (p == null) {
			p = "p";
		} else {
			p = p.trim();
		}
		this.p = p;
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}
}
