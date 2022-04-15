Id cvId = '0687a000001gikmAAA';

createShare(cvId);

public static void createShare(Id cvId) {
    User user = [SELECT Id FROM User WHERE Username LIKE 'cloud@%' LIMIT 1];
    Id conDocId = [SELECT ContentDocumentId FROM ContentVersion WHERE Id =:cvId].ContentDocumentId;

    ContentDocumentLink cdl = new ContentDocumentLink();
    cdl.ContentDocumentId = conDocId;
    cdl.LinkedEntityId = user.Id;
    cdl.ShareType = 'V';
    insert cdl;
}