import React from "react";
import { ImportContacts } from "./ImportContacts";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ImportContacts",
  component: ImportContacts,
};

export const Default = () => <ImportContacts />;
export const Fill = () => <ImportContacts fill="blue" />;
export const Size = () => <ImportContacts height="50" width="50" />;
export const CustomStyle = () => <ImportContacts style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ImportContacts className="custom-class" />;

export const Clickable = () => <ImportContacts onClick={()=>console.log("clicked")} />;

const Template = (args) => <ImportContacts {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
