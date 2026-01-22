import React from "react";
import { ContactPage } from "./ContactPage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ContactPage",
  component: ContactPage,
};

export const Default = () => <ContactPage />;
export const Fill = () => <ContactPage fill="blue" />;
export const Size = () => <ContactPage height="50" width="50" />;
export const CustomStyle = () => <ContactPage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ContactPage className="custom-class" />;

export const Clickable = () => <ContactPage onClick={()=>console.log("clicked")} />;

const Template = (args) => <ContactPage {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
