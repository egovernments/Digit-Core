import React from "react";
import { Attachment } from "./Attachment";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Attachment",
  component: Attachment,
};

export const Default = () => <Attachment />;
export const Fill = () => <Attachment fill="blue" />;
export const Size = () => <Attachment height="50" width="50" />;
export const CustomStyle = () => <Attachment style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Attachment className="custom-class" />;
export const Clickable = () => <Attachment onClick={()=>console.log("clicked")} />;

const Template = (args) => <Attachment {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
