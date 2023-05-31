import { Component, OnInit } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { AuthService } from '../../../shared/services/auth.service';
import { CategoriesService } from '../../services/categories.service';
import { Category } from '../../../shared/model/category';

@Component({
    selector: 'app-categories',
    templateUrl: './categories.component.html'
})
export class CategoriesComponent implements OnInit {
    categories$: Observable<Category[]> | undefined;
    toggled = false;
    category: BehaviorSubject<Category | null> =
        new BehaviorSubject<Category | null>(null);

    constructor(
        private categoriesService: CategoriesService,
        protected authService: AuthService
    ) {}

    getCategories() {
        this.categories$ = this.categoriesService.getAllCategories();
    }

    ngOnInit() {
        this.getCategories();
    }

    toggleRates(category: Category) {
        if (!this.toggled) {
            this.toggled = !this.toggled;
        }
        this.category.next(category);
    }

    scroll(el: HTMLElement | null) {
        el?.scrollIntoView({ behavior: 'smooth' });
    }

    protected readonly document = document;
}
