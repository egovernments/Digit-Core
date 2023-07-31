import React from "react";
import { MiscellaneousServices } from "./MiscellaneousServices";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MiscellaneousServices",
  component: MiscellaneousServices,
};

export const Default = () => <MiscellaneousServices />;
export const Fill = () => <MiscellaneousServices fill="blue" />;
export const Size = () => <MiscellaneousServices height="50" width="50" />;
export const CustomStyle = () => <MiscellaneousServices style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MiscellaneousServices className="custom-class" />;

export const Clickable = () => <MiscellaneousServices onClick={()=>console.log("clicked")} />;

const Template = (args) => <MiscellaneousServices {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
