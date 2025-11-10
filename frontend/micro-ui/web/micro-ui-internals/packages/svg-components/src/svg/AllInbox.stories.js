import React from "react";
import { AllInbox } from "./AllInbox";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AllInbox",
  component: AllInbox,
};

export const Default = () => <AllInbox />;
export const Fill = () => <AllInbox fill="blue" />;
export const Size = () => <AllInbox height="50" width="50" />;
export const CustomStyle = () => <AllInbox style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AllInbox className="custom-class" />;
export const Clickable = () => <AllInbox onClick={()=>console.log("clicked")} />;

const Template = (args) => <AllInbox {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
