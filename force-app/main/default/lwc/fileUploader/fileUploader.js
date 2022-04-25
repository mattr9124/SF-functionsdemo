/**
 * Created by mrossner on 15/04/2022.
 */

import {LightningElement} from 'lwc';
import processFileUpload from '@salesforce/apex/FileUploadHandler.processFileUpload';
import createShare from '@salesforce/apex/FileUploadHandler.createShare'

export default class FileUploader extends LightningElement {

    parsedResult;

    afterFileUpload(event) {
        const uploadedFiles = event.detail.files;
        console.log('uploadedFiles', uploadedFiles);

        const versionId = uploadedFiles[0].contentVersionId;
        console.log('versionId', versionId);

        createShare(({contentVersionId: versionId})).then(result => {
            processFileUpload({contentVersionId: versionId}).then(result => {
                console.log('Result is', result);
                this.parsedResult = result;
            }).catch(error => {
                console.log('An error occurred', error);
                this.parsedResult = error;
            })
        }).catch(error => {
            console.log('An error occurred', error);
            this.parsedResult = error;
        });
    }
}