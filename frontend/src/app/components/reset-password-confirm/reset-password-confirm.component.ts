import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AccountService } from '../../services/account.service';
import { ActivatedRoute } from '@angular/router';

@Component({
    selector: 'app-reset-password-confirm',
    templateUrl: './reset-password-confirm.component.html'
})
export class ResetPasswordConfirmComponent {
    resetPasswordForm = new FormGroup({
        password: new FormControl('', [
            Validators.required,
            Validators.minLength(8),
            Validators.pattern(
                '^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[\\-!_$@?()\\[\\]#%])[A-Za-z0-9\\-!_$@?()\\[\\]#%]{8,}$'
            )
        ]),
        repeatPassword: new FormControl('', [
            Validators.required,
            Validators.minLength(8),
            Validators.pattern(
                '^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[\\-!_$@?()\\[\\]#%])[A-Za-z0-9\\-!_$@?()\\[\\]#%]{8,}$'
            )
        ])
    });

    loading = false;
    token = '';
    showPassword = false;
    showRepeatPassword = false;
    regexp = new RegExp(
        '^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$'
    );

    constructor(
        private accountService: AccountService,
        private route: ActivatedRoute
    ) {
        this.route.paramMap.subscribe((params) => {
            const param = params.get('token');
            if (param != null) {
                this.token = param.toString();
            }
        });
    }

    get password() {
        return this.resetPasswordForm.get('password');
    }

    get repeatPassword() {
        return this.resetPasswordForm.get('repeatPassword');
    }

    onSubmit() {
        this.loading = true;
        const password = this.resetPasswordForm.getRawValue().password;
        const repeatPassword =
            this.resetPasswordForm.getRawValue().repeatPassword;

        if (password !== repeatPassword) {
            return;
        }

        if (this.resetPasswordForm.valid) {
            const resetPasswordDTO: object = {
                password: this.resetPasswordForm.getRawValue().password,
                token: this.token
            };
            this.accountService.resetPasswordConfirm(resetPasswordDTO);
        }
    }
}
