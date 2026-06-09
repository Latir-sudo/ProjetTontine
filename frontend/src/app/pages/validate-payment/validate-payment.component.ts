import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-validate-payment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './validate-payment.component.html',
  styleUrls: ['./validate-payment.component.scss']
})
export class ValidatePaymentComponent {
  payment = {
    amount: 5000,
    date: '',
    time: '',
    reference: ''
  };

  constructor(private router: Router) {}

  submit() {
    this.router.navigate(['/notifications']);
  }
}
