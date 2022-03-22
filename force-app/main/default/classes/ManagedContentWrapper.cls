@RestResource(UrlMapping='/cmswrapper/getmanagedcontent')
global without sharing class ManagedContentWrapper {
    @HttpGet
    global static String getAllManagedContent() {

        String communityId = RestContext.request.params.get('communityId');
        String language = RestContext.request.params.get('language');

        return JSON.serialize(
                ConnectApi.ManagedContent.getAllManagedContent
                        (communityId, 0, 20, language, 'cms_image', true)
        );
    }
}