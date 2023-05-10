import { Component } from '@angular/core';
import { ChangeEmailService } from '../../services/change-email.service';
import { ToastService } from '../../services/toast.service';

@Component({
    selector: 'app-change-email',
    templateUrl: './change-email.component.html'
})
export class ChangeEmailComponent {
    constructor(
        private emailService: ChangeEmailService,
        private toastService: ToastService
    ) {}

    onClick() {
        this.emailService.changeEmail().subscribe((result: boolean) => {
            if (result) {
                this.toastService.showSuccess('Mail has been sent.'); //todo i18n
            } else {
                this.toastService.showDanger('Something went wrong.'); //todo i18n
            }
        });
    }
}