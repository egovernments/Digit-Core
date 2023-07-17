import React from "react";
import { DesignServices } from "./DesignServices";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DesignServices",
  component: DesignServices,
};

export const Default = () => <DesignServices />;
export const Fill = () => <DesignServices fill="blue" />;
export const Size = () => <DesignServices height="50" width="50" />;
export const CustomStyle = () => <DesignServices style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DesignServices className="custom-class" />;

export const Clickable = () => <DesignServices onClick={()=>console.log("clicked")} />;

const Template = (args) => <DesignServices {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
