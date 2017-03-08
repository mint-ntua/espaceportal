package sources.core;

import java.util.ArrayList;
import java.util.List;

import model.resources.WithResource;

public class ItemsGrouping {
	
	//TODO: When Within search is separated from external resources, 
	//support all types of resources. Generic, for all entries of WithResourceType enum.
	private List<WithResource<?, ?>> culturalCHO;
	private List<WithResource<?, ?>> recordResource;

	public ItemsGrouping() {
		super();
		culturalCHO = new ArrayList<>();
		recordResource = new ArrayList<>();
	}

	public List<WithResource<?, ?>> getCulturalCHO() {
		return culturalCHO;
	}

	public void setCulturalCHO(List<WithResource<?, ?>> culturalHO) {
		this.culturalCHO = culturalHO;
	}
	
	public List<WithResource<?, ?>> getRecordResource() {
		return recordResource;
	}

	public void setRecordResource(List<WithResource<?, ?>> recordResource) {
		this.recordResource = recordResource;
	}

	public void addAll(ItemsGrouping items) {
		culturalCHO.addAll(items.getCulturalCHO());
		recordResource.addAll(items.getRecordResource());
	}

	public int getItemsCount() {
		return culturalCHO.size()+recordResource.size();
	}

}
