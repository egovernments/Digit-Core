import React from "react";
import { DomainVerification } from "./DomainVerification";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DomainVerification",
  component: DomainVerification,
};

export const Default = () => <DomainVerification />;
export const Fill = () => <DomainVerification fill="blue" />;
export const Size = () => <DomainVerification height="50" width="50" />;
export const CustomStyle = () => <DomainVerification style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DomainVerification className="custom-class" />;

export const Clickable = () => <DomainVerification onClick={()=>console.log("clicked")} />;

const Template = (args) => <DomainVerification {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
