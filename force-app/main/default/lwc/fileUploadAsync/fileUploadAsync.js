import {LightningElement} from 'lwc';
import {subscribe} from 'lightning/empApi';
import processFileUpload from '@salesforce/apex/FileUploadHandlerAsync.processFileUpload';

export default class FileUploadAsync extends LightningElement {

    parsedResult;

    connectedCallback() {
        const messageCallback = (response) => {
            console.log('received event', response);
            this.parsedResult = response.data.payload.Payload__c;
        }

        subscribe('/event/UploadEvent__e', -1, messageCallback).then(r => console.log('subscribed', r));
    }

    afterFileUpload(event) {
        const uploadedFiles = event.detail.files;
        console.log('uploadedFiles', uploadedFiles);

        const versionId = uploadedFiles[0].contentVersionId;
        console.log('versionId', versionId);

        processFileUpload({contentVersionId: versionId}).then(result => {
            console.log('Result is', result);
            this.parsedResult = result;
        }).catch(error => {
            console.log('An error occurred', error);
            this.parsedResult = error;
        });
    }
}