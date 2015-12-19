package org.but4reuse.feature.location.tf;

import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adaptedmodel.AdaptedModel;
import org.but4reuse.adaptedmodel.Block;
import org.but4reuse.adaptedmodel.helpers.AdaptedModelHelper;
import org.but4reuse.adapters.IElement;
import org.but4reuse.feature.location.IFeatureLocation;
import org.but4reuse.feature.location.LocatedFeature;
import org.but4reuse.feature.location.LocatedFeaturesUtils;
import org.but4reuse.feature.location.impl.StrictFeatureSpecificFeatureLocation;
import org.but4reuse.featurelist.Feature;
import org.but4reuse.featurelist.FeatureList;
import org.but4reuse.wordclouds.util.TermFrequencyUtils;
import org.eclipse.core.runtime.IProgressMonitor;

public class SFS_TF implements IFeatureLocation {

	@Override
	public List<LocatedFeature> locateFeatures(FeatureList featureList, AdaptedModel adaptedModel,
			IProgressMonitor monitor) {

		// Get SFS results, all located feature are 1 confidence
		StrictFeatureSpecificFeatureLocation sfs = new StrictFeatureSpecificFeatureLocation();
		List<LocatedFeature> sfsLocatedBlocks = sfs.locateFeatures(featureList, adaptedModel, monitor);

		List<LocatedFeature> locatedFeatures = new ArrayList<LocatedFeature>();

		// Get all the features of a given block and all its elements
		for (Block block : adaptedModel.getOwnedBlocks()) {
			monitor.subTask("Feature location FCA SFS and Term Frequency. Features competing for Elements at " + block.getName());
			List<Feature> blockFeatures = LocatedFeaturesUtils.getFeaturesOfBlock(sfsLocatedBlocks, block);
			List<IElement> blockElements = AdaptedModelHelper.getElementsOfBlock(block);

			// For each element, we associate it to the feature with higher tf
			for (IElement e : blockElements) {
				int maxTFfound = 0;
				List<Feature> maxFeatures = new ArrayList<Feature>();
				for (Feature f : blockFeatures) {
					int tf = TermFrequencyUtils.calculateTermFrequency(f, e);
					if (tf == maxTFfound) {
						maxFeatures.add(f);
					} else if (tf > maxTFfound) {
						maxFeatures.clear();
						maxFeatures.add(f);
						maxTFfound = tf;
					}
				}
				// Add to the located features
				for (Feature f : maxFeatures) {
					locatedFeatures.add(new LocatedFeature(f, e, 1));
				}
			}
		}

		// System.out.println(locatedFeatures.size());
		return locatedFeatures;
	}

}