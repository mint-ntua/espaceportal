package model.annotations.selectors;

import model.annotations.Annotation;

import org.mongodb.morphia.query.Query;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ImageSVGSelector extends SelectorType {
	
	private String imageWithURL;
	
	private String text;
	private String format;
	
	
	@Override
    public Object clone() throws CloneNotSupportedException {
		ImageSVGSelector c = (ImageSVGSelector)super.clone();
		c.imageWithURL = imageWithURL;
		c.text = text;
		c.format = format;

		return c;
    }
	
	@Override
	public void addToQuery(Query<Annotation> q) {
		q.field("target.selector.imageWithURL").equal(imageWithURL);
		
		if (text != null) {
			q.field("target.selector.text").equal(text);
		}
		
		if (format != null) {
			q.field("target.selector.format").equal(format);
		}
	}


	public String getImageWithURL() {
		return imageWithURL;
	}


	public void setImageWithURL(String imageWithURL) {
		this.imageWithURL = imageWithURL;
	}


	public String getSvg() {
		return text;
	}


	public void setSvg(String text) {
		this.text = text;
	}


	public String getFormat() {
		return format;
	}


	public void setFormat(String format) {
		this.format = format;
	}


}
