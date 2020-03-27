package fr.paris.lutece.plugins.sponsoredlink.solr.indexer;


import fr.paris.lutece.plugins.search.solr.business.field.Field;
import fr.paris.lutece.plugins.search.solr.indexer.SolrIndexer;
import fr.paris.lutece.plugins.search.solr.indexer.SolrIndexerService;
import fr.paris.lutece.plugins.search.solr.indexer.SolrItem;
import fr.paris.lutece.plugins.search.solr.util.SolrConstants;
import fr.paris.lutece.plugins.sponsoredlinks.business.SponsoredLink;
import fr.paris.lutece.plugins.sponsoredlinks.business.SponsoredLinkSet;
import fr.paris.lutece.plugins.sponsoredlinks.business.SponsoredLinkSetHome;
import fr.paris.lutece.plugins.sponsoredlinks.service.SponsoredLinksPlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.url.UrlItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SponsoredLinksSolrIndexer implements SolrIndexer {

    public static final String PROPERTY_INDEXER_NAME = "sponsoredlink-solr.indexer.name";

    // idSolrIndexer
    private static final String PARAMETER_DOCUMENT_ID = "document_id";
    private static final String PARAMETER_BLOG_ID = "id";
    private static final String PARAMETER_HTMLPAGE_ID = "htmlpage_id";

    private static final Plugin PLUGIN_SPONSOREDLINK = PluginService.getPlugin( SponsoredLinksPlugin.PLUGIN_NAME );
    private static final String SPONSORED = "sponsored";

    public static final String CONSTANT_TYPE_RESOURCE_BLOG = "BLOG_DOCUMENT";
    public static final String CONSTANT_TYPE_RESOURCE_DOC = "DOCUMENT_DOCUMENT";
    public static final String CONSTANT_TYPE_RESOURCE_HTMLPAGE = "HTMLPAGE_HTMLPAGE";


    private static final String PROPERTY_INDEXER_DESCRIPTION = "sponsoredlink-solr.indexer.description";
    private static final String PROPERTY_INDEXER_VERSION = "sponsoredlink-solr.indexer.version";
    private static final String PROPERTY_INDEXER_ENABLE = "sponsoredlink-solr.indexer.enable";
    private static final String DOC_INDEXATION_ERROR = "[SolrDocIndexer] An error occured during the indexation ";
    private static final String SHORT_NAME_BLOG = "blog";
    private static final String SHORT_NAME_DOCUMENT = "doc";
    private static final String SHORT_NAME_HTMALPAGE = "hpg";

    @Override
    public List<String> indexDocuments() {

        List<String> lstErrors = new ArrayList<String>( );
        Plugin plugin = PluginService.getPlugin( SponsoredLinksPlugin.PLUGIN_NAME );

        Collection<SponsoredLinkSet> listSets = SponsoredLinkSetHome.findAll( plugin );
        Collection<String> listSponsoredUrl = getSponsoredLinkUrlList();

        for (String s : listSponsoredUrl) {
            try {
                SolrItem item = getItem(s);

                if (item != null) {
                    SolrIndexerService.update(item);
                }
            } catch (Exception e) {
                lstErrors.add(SolrIndexerService.buildErrorMessage(e));
                AppLogService.error(DOC_INDEXATION_ERROR, e);
            }
        }
        return lstErrors;
    }

    private SolrItem getItem(String strUrl ) {
        SolrItem item = null;
        item = new SolrItem();

        if (strUrl.contains("?"+PARAMETER_BLOG_ID))
        {
            item.setUid(getResourceUid(Integer.valueOf(strUrl.substring(strUrl.indexOf("?"+PARAMETER_BLOG_ID)+("?"+PARAMETER_BLOG_ID).length()+1,
                    strUrl.indexOf("&"))).toString(),CONSTANT_TYPE_RESOURCE_BLOG));
        }
        else
        {

            if (strUrl.contains(PARAMETER_DOCUMENT_ID))
            {
                item.setUid(getResourceUid(Integer.valueOf(strUrl.substring(strUrl.indexOf(PARAMETER_DOCUMENT_ID)+PARAMETER_DOCUMENT_ID.length()+1,
                        strUrl.indexOf("&"))).toString(),CONSTANT_TYPE_RESOURCE_DOC));
            }
            else
            {
                if (strUrl.contains(PARAMETER_HTMLPAGE_ID))
                {
                    item.setUid(getResourceUid(Integer.valueOf(strUrl.substring(strUrl.indexOf(PARAMETER_HTMLPAGE_ID)+PARAMETER_HTMLPAGE_ID.length()+1)).toString(),
                            CONSTANT_TYPE_RESOURCE_HTMLPAGE));
                }
            }
        }

        item.addDynamicField(SPONSORED, 1L);

        return item;
    }

    @Override
    public String getName() {
        return AppPropertiesService.getProperty( PROPERTY_INDEXER_NAME );
    }

    @Override
    public String getVersion() {
        return AppPropertiesService.getProperty( PROPERTY_INDEXER_VERSION );
    }

    @Override
    public String getDescription() {
        return AppPropertiesService.getProperty( PROPERTY_INDEXER_DESCRIPTION );
    }

    @Override
    public boolean isEnable() {
        return "true".equalsIgnoreCase( AppPropertiesService.getProperty(PROPERTY_INDEXER_ENABLE));
    }

    @Override
    public List<Field> getAdditionalFields() {
        return null;
    }

    @Override
    public List<SolrItem> getDocuments(String s) {
        return null;
    }

    @Override
    public List<String> getResourcesName() {
        return null;
    }

    @Override
    public String getResourceUid( String strResourceId, String strResourceType)
    {
        StringBuffer sb = new StringBuffer( strResourceId );
        if (strResourceType == CONSTANT_TYPE_RESOURCE_BLOG) {
            sb.append( SolrConstants.CONSTANT_UNDERSCORE ).append( SHORT_NAME_BLOG );
        }

        if (strResourceType == CONSTANT_TYPE_RESOURCE_DOC) {
            sb.append( SolrConstants.CONSTANT_UNDERSCORE ).append( SHORT_NAME_DOCUMENT );
        }

        if (strResourceType == CONSTANT_TYPE_RESOURCE_HTMLPAGE) {
            sb.append( SolrConstants.CONSTANT_UNDERSCORE ).append( SHORT_NAME_HTMALPAGE );
        }

        return sb.toString( );
    }

    private Collection<String> getSponsoredLinkUrlList() {
        Collection<SponsoredLinkSet> listSets = SponsoredLinkSetHome.findAll(PLUGIN_SPONSOREDLINK);

        List<String> listSponsoredUrl = new ArrayList<>();

        if (listSets!=null)
        {
            for ( SponsoredLinkSet currentSet : listSets )
            {
                SponsoredLinkSet set = SponsoredLinkSetHome.findByPrimaryKey( currentSet.getId( ), PLUGIN_SPONSOREDLINK);

                for ( SponsoredLink link : set.getSponsoredLinkList( ) )
                {
                    listSponsoredUrl.add(new UrlItem( link.getLinkAttribute( SponsoredLink.HREF ) ).getUrl());
                }
            }

        }
        return listSponsoredUrl;
    }

}
